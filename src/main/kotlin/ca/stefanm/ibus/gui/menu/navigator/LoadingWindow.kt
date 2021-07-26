package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.desktop.AppManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.window.v1.KeyStroke
import ca.stefanm.ibus.gui.debug.DeviceConfigurationViewerWindow
import ca.stefanm.ibus.gui.debug.KeyEventSimulator
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.gui.debug.DebugLaunchpad
import ca.stefanm.ibus.gui.debug.PaneManagerDebug
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalTime
class LoadingWindow @Inject constructor(

    private val deviceConfigurationViewerWindow: DeviceConfigurationViewerWindow,
    private val configurablePlatform: ConfigurablePlatform,
    private val keyEventSimulator: KeyEventSimulator,
    private val debugLaunchpad: DebugLaunchpad,
    private val paneManagerDebug: PaneManagerDebug,
    private val menuWindow: MenuWindow,

    private val windowManager : Provider<WindowManager>
) {

    fun contents() : @Composable WindowScope.() -> Unit = {

        MenuBar {

            Menu("Quit") {
                Item("Quit",
                    onClick = { AppManager.exit() },
                    //shortcut = KeyStroke(Key.Q)
                )
            }

            Menu("Platform") {
                Item("Start Platform",
                    onClick = { configurablePlatform.run() },
                    //shortcut = KeyStroke(Key.S)
                )
                Item("Stop Platform",
                    onClick = { configurablePlatform.stop() },
                    //shortcut = KeyStroke(Key.E)
                )
                Item("Restart Platform",
                    onClick = { configurablePlatform.stop(); configurablePlatform.run() },
                    //shortcut = KeyStroke(Key.R)
                )
            }
            Menu("Debug") {
                Item("Key Event Simulator",
                    onClick = { windowManager.get().openDebugWindow(keyEventSimulator) },
                    //shortcut = KeyStroke(Key.K)
                )
                Item("Debug Launchpad",
                    onClick = { windowManager.get().openDebugWindow(debugLaunchpad) },
                    //shortcut = KeyStroke(Key.D)
                )
                Item("Pane Manager Debugger",
                    onClick = { windowManager.get().openDebugWindow(paneManagerDebug) },
                    //shortcut = KeyStroke(Key.P)
                )
            }
            Menu("Configuration") {
                Item("View Device Configuration",
                    onClick = { deviceConfigurationViewerWindow.show(configurablePlatform.currentConfiguration) }
                )
            }
        }

        Image(
            bitmap = imageFromResource("bmw_navigation.png"),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
                .background(Color.Black)
                .clickable {
                    openMenuWindow()
                }
        )

//        LaunchedEffect(true) {
//            delay(2000)
//            openMenuWindow()
//        }
    }

    private fun openMenuWindow() {
        windowManager.get().openHmiMainWindow()
    }
}