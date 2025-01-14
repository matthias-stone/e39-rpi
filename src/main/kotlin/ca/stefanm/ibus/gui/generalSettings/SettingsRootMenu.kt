package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.debug.hmiScreens.DebugHmiRoot
import ca.stefanm.ibus.gui.map.settings.MapTileDownloaderScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class SettingsRootMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SettingsRootMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Settings")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.navigateToRoot()
                        }
                    ),
                    TextMenuItem(
                        title = "Bluetooth",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(
                                BluetoothPairingMenu::class.java
                            )
                        }
                    ),
                    TextMenuItem(
                        title = "Map Tile Downloader",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(
                                MapTileDownloaderScreen::class.java
                            )
                        }
                    ),
                    TextMenuItem(
                        title = "Car Platform Config",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(
                                CarPlatformConfigScreen::class.java
                            )
                        }
                    ),
                    TextMenuItem(
                        title = "About",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(
                                AboutScreen::class.java
                            )
                        }
                    )
                )
            )
        }
    }
}