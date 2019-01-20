package com.faforever.client.chat;

import com.faforever.client.avatar.Avatar;
import com.faforever.client.avatar.AvatarImageService;
import com.faforever.client.avatar.AvatarService;
import com.faforever.client.game.Game;
import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.ImmediateNotification;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.play.JoinGameHelper;
import com.faforever.client.player.Player;
import com.faforever.client.player.PlayerService;
import com.faforever.client.player.SocialStatus;
import com.faforever.client.preferences.ChatColorMode;
import com.faforever.client.preferences.ChatPrefs;
import com.faforever.client.preferences.Preferences;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.replay.ReplayService;
import com.faforever.client.test.AbstractPlainJavaFxTest;
import com.faforever.client.theme.UiService;
import com.google.common.eventbus.EventBus;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testfx.util.WaitForAsyncUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChatChannelUserContextMenuControllerTest extends AbstractPlainJavaFxTest {
  private static final String TEST_USER_NAME = "junit";

  @Mock
  private ChatService chatService;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private UiService uiService;
  @Mock
  private PlayerService playerService;
  @Mock
  private ReplayService replayService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private I18n i18n;
  @Mock
  private EventBus eventBus;
  @Mock
  private JoinGameHelper joinGameHelper;
  @Mock
  private AvatarService avatarService;
  @Mock
  private AvatarImageService avatarImageService;

  private ChatUserContextMenuController instance;
  private Player player;
  private ChatChannelUser chatUser;

  @Before
  public void setUp() throws Exception {
    instance = new ChatUserContextMenuController(preferencesService, playerService,
        replayService, notificationService, i18n, eventBus, joinGameHelper, avatarService, avatarImageService, uiService);

    Preferences preferences = mock(Preferences.class);
    ChatPrefs chatPrefs = mock(ChatPrefs.class);
    ObjectProperty<ChatColorMode> chatColorModeObjectProperty = new SimpleObjectProperty<>();

    when(preferencesService.getPreferences()).thenReturn(preferences);
    when(preferences.getChat()).thenReturn(chatPrefs);
    when(chatPrefs.getUserToColor()).thenReturn(mock(ObservableMap.class));
    when(chatPrefs.chatColorModeProperty()).thenReturn(chatColorModeObjectProperty);
    when(avatarService.getAvailableAvatars(any())).thenReturn(CompletableFuture.completedFuture(Arrays.asList(
        new Avatar(1, new URL("http://www.example.com/avatar1.png"), "Avatar Number #1"),
        new Avatar(2, new URL("http://www.example.com/avatar2.png"), "Avatar Number #2"),
        new Avatar(3, new URL("http://www.example.com/avatar3.png"), "Avatar Number #3")
    )));


    loadFxml("theme/chat/chat_user_context_menu.fxml", clazz -> instance);

    player = new Player(TEST_USER_NAME);
    player.setSocialStatus(SocialStatus.SELF);
    player.setGame(new Game());

    chatUser = new ChatChannelUser(TEST_USER_NAME,null, false);
    chatUser.setPlayer(player);
    instance.setChatUser(chatUser);
  }

  @Test
  public void testOnSendPrivateMessage() {
    instance.onSendPrivateMessageSelected();

    verify(eventBus).post(any(InitiatePrivateChatEvent.class));
  }

  @Test
  public void testOnAddFriendWithFoe() {
    player.setSocialStatus(SocialStatus.FOE);

    instance.onAddFriendSelected();

    verify(playerService).removeFoe(player);
    verify(playerService).addFriend(player);
  }

  @Test
  public void testOnAddFriendWithNeutral() {
    player.setSocialStatus(SocialStatus.OTHER);

    instance.onAddFriendSelected();

    verify(playerService, never()).removeFoe(player);
    verify(playerService).addFriend(player);
  }

  @Test
  public void testOnRemoveFriend() {
    instance.onRemoveFriendSelected();

    verify(playerService).removeFriend(player);
  }

  @Test
  public void testOnAddFoeWithFriend() {
    player.setSocialStatus(SocialStatus.FRIEND);

    instance.onAddFoeSelected();

    verify(playerService).removeFriend(player);
    verify(playerService).addFoe(player);
  }

  @Test
  public void testOnAddFoeWithNeutral() {
    player.setSocialStatus(SocialStatus.OTHER);

    instance.onAddFoeSelected();

    verify(playerService, never()).removeFriend(player);
    verify(playerService).addFoe(player);
  }

  @Test
  public void testOnRemoveFoe() {
    instance.onRemoveFoeSelected();

    verify(playerService).removeFoe(player);
  }

  @Test
  public void testOnWatchGame() {
    instance.onWatchGameSelected();

    verify(replayService).runLiveReplay(player.getGame().getId(), player.getId());
  }

  @Test
  public void testOnWatchGameThrowsIoExceptionTriggersNotification() {
    doThrow(new RuntimeException("Error in runLiveReplay")).when(replayService).runLiveReplay(anyInt(), anyInt());

    instance.onWatchGameSelected();

    verify(notificationService).addNotification(any(ImmediateNotification.class));
  }

  @Test
  public void testOnJoinGame() {
    instance.onJoinGameSelected();

    verify(joinGameHelper).join(any());
  }

  @Test
  public void onSelectAvatar() throws Exception {
    instance.avatarComboBox.show();

    WaitForAsyncUtils.waitForAsyncFx(100_000, () -> instance.avatarComboBox.getSelectionModel().select(2));

    ArgumentCaptor<Avatar> captor = ArgumentCaptor.forClass(Avatar.class);
    verify(avatarService).changeAvatar(captor.capture());

    Avatar avatar = captor.getValue();
    assertThat(avatar.getUrl(), equalTo(new URL("http://www.example.com/avatar2.png")));
    assertThat(avatar.getDescription(), is("Avatar Number #2"));
  }

  @Test
  public void testHideUserInfoIfNoPlayer() {
    chatUser.setPlayer(null);
    instance.setChatUser(chatUser);
    assertThat(instance.showUserInfo.isVisible(), is(false));
  }
}