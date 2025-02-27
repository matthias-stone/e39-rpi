package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.LogDistributionHub
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.E


@AutoDiscover
@ApplicationScope
class HmiLogViewerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val logDistributionHub: LogDistributionHub,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = HmiLogViewerScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val messages = remember { mutableStateListOf<LogDistributionHub.LogEvent>() }
        val observer : (LogDistributionHub.LogEvent) -> Unit = remember {
            { event -> messages.add(event) }
        }
        LaunchedEffect(true) {
            logDistributionHub.registerObserver(observer)
        }

        DisposableEffect(Unit) {
            onDispose {
                logDistributionHub.unregisterObserver(observer)
            }
        }

        Column {
            HalfScreenMenu.OneColumn(items = listOf(
                TextMenuItem(
                    title = "Go Back",
                    onClicked = { navigationNodeTraverser.goBack() }
                )
            ))


            val stateVertical = rememberScrollState(0)

            LaunchedEffect(messages.size) {
                stateVertical.scrollTo(stateVertical.maxValue)
            }


//            Column(
//                Modifier.fillMaxSize().background(Color.Cyan)
//            ) {
//                for (message in messages) {
//                        Text(
//                            text = message.toMessage(),
//                            color = ChipItemColors.TEXT_WHITE,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Light
//                        )
//                }
//            }

            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(stateVertical)
                        .background(ChipItemColors.MenuBackground)
                        .padding(20.dp)
                ) {

                    Column(
                        Modifier.wrapContentHeight()
                    ) {
                        for (message in messages) {
                            Column {
                                Text(
                                    text = message.toMessage(),
                                    color = ChipItemColors.TEXT_WHITE,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    //todo someday this could be a composable Text()
    private fun LogDistributionHub.LogEvent.toMessage() : String {
        return when (level) {
           LogDistributionHub.LogEvent.Level.V -> "VERBOSE : $tag / $message"
           LogDistributionHub.LogEvent.Level.D -> "DEBUG : $tag / $message"
           LogDistributionHub.LogEvent.Level.I -> "INFO : $tag / $message"
           LogDistributionHub.LogEvent.Level.W -> "WARN : $tag / $message"
           LogDistributionHub.LogEvent.Level.E -> {
               if (exception == null) {
                   "ERROR : $tag / $message"
               } else {
                   "ERROR : $tag / $message exception:: ${exception.stackTraceToString()}"
               }
           }
        }
    }
}