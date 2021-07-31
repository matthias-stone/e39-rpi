package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.*
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.SPACER
import javax.inject.Inject

@ApplicationScope
@Stable
class DebugHmiRoot @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiRoot::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Debug")

            val isBottomShowing = remember { mutableStateOf(false) }
            val isTopShowing = remember { mutableStateOf(true) }

            val leftItems = listOf(
                TextMenuItem(
                    title = "Go Back",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),
                TextMenuItem(
                    title = "Debug 2",
                    onClicked = { navigationNodeTraverser.navigateToNode(DebugScreen2::class.java) }
                ),
                CheckBoxMenuItem(
                    title = "Half Screen Top",
                    isChecked = isTopShowing.value,
                    onClicked = {
                        isTopShowing.value = !isTopShowing.value
                    }
                ),
                SPACER,
                CheckBoxMenuItem(
                    title = "Half Screen Bottom",
                    isChecked = isBottomShowing.value,
                    onClicked = {
                        isBottomShowing.value = !isBottomShowing.value
                    }
                ),
                TextMenuItem(
                    title = "Open Modal",
                    onClicked = {
                        modalMenuService.showModalMenu(
                            menuTopLeft = IntOffset(300, 200),
                            menuWidth = 400,
                            menuData = ModalMenu(
                                chipOrientation = ItemChipOrientation.E,
                                items = listOf(
                                    ModalMenu.ModalMenuItem(
                                        title = "First",
                                        onClicked = {
                                            notificationHub.postNotificationBackground(
                                                Notification(
                                                    Notification.NotificationImage.ALERT_CIRCLE,
                                                    topText = "FIRST!!"
                                                )
                                            )
                                        }
                                    ),
                                    ModalMenu.ModalMenuItem(
                                        title = "Second",
                                        onClicked = {modalMenuService.closeModalMenu()}
                                    )
                                )
                            )
                        )
                    }
                ),
                TextMenuItem(
                    title = "Modal 2",
                    onClicked = {
                        modalMenuService.showModalMenu(
                            menuTopLeft = IntOffset(800, 200),
                            menuWidth = 400,
                            menuData = ModalMenu(
                                chipOrientation = ItemChipOrientation.E,
                                items = listOf(
                                    ModalMenu.ModalMenuItem(
                                        title = "Third",
                                        onClicked = {
                                            notificationHub.postNotificationBackground(
                                                Notification(
                                                    Notification.NotificationImage.ALERT_CIRCLE,
                                                    topText = "FIRST!!"
                                                )
                                            )
                                        }
                                    ),
                                    ModalMenu.ModalMenuItem(
                                        title = "Fourth",
                                        onClicked = {modalMenuService.closeModalMenu()}
                                    )
                                )
                            )
                        )
                    }
                )
            )

            val rightItems = (1..4).map {
                TextMenuItem(
                    title = "Item: $it",
                    isSelectable = it != 2,
                    onClicked = {}
                )
            }

            Box(Modifier.wrapContentWidth().fillMaxSize()) {
                FullScreenMenu.TwoColumnFillFromTop(
                    leftItems = leftItems,
                    rightItems = rightItems
                )
//
//                HalfScreenMenu.OneColumn(
//                    items = leftItems,
//                    alignment = Alignment.Start
//                )

                if (isTopShowing.value) {
                    //HalfScreenMenu.TopHalfTwoColumn(leftItems, rightItems)
                }

                if (isBottomShowing.value) {
//                    HalfScreenMenu.BottomHalfTwoColumn(leftItems, rightItems)
                }
            }
        }
    }
}

@ApplicationScope
class DebugScreen2 @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugScreen2::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column {
            BmwSingleLineHeader("Debug 2")

            HalfScreenMenu.OneColumn(
                items = mutableListOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    ),
                    TextMenuItem(
                        title = "2",
                        onClicked = {
                            notificationHub.postNotificationBackground(
                                Notification(
                                    image = Notification.NotificationImage.ALERT_OCTAGON,
                                    topText = "2!"
                            ))
                        }
                    ),
                    TextMenuItem(
                        title = "3",
                        onClicked = {
                            modalMenuService.showModalMenu(
                                menuTopLeft = IntOffset(500, 300),
                                menuWidth = 500,
                                menuData = ModalMenu(
                                    chipOrientation = ItemChipOrientation.E,
                                    items = listOf(
                                        ModalMenu.ModalMenuItem(
                                            title = "1",
                                            onClicked = { notificationHub.postNotificationBackground(Notification(Notification.NotificationImage.ALERT_TRIANGLE, topText = "1")) }
                                        ),
                                        ModalMenu.ModalMenuItem(
                                            title = "2",
                                            onClicked = { notificationHub.postNotificationBackground(Notification(Notification.NotificationImage.ALERT_TRIANGLE, topText = "2"))}
                                        )
                                    )
                                )
                            )
                        }
                    ),
                    TextMenuItem(title = "4", onClicked = {}),
                    TextMenuItem(title = "5", onClicked = {}),
                    TextMenuItem(title = "6", onClicked = {}),
                    TextMenuItem(title = "7", onClicked = {}),
                ),
                alignment = Alignment.Start
            )

        }
    }
}