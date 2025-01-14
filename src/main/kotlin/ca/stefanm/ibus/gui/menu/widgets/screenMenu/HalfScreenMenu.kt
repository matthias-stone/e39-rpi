package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem.Companion.reduceUpdateOnClick
import kotlinx.coroutines.flow.MutableSharedFlow
import org.intellij.lang.annotations.JdkConstants
import kotlin.math.E


object HalfScreenMenu {

    /**
     *      -----------------
     *      '(open to below)'
     *      '---------------'
     *      ' ~~         ~~ '
     *      ' ~~         ~~ '
     *      -----------------
     */
    @Composable
    fun BottomHalfTwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Bottom)
        ) {
                TwoColumn(leftItems, rightItems)
        }
    }

    /**
     *      -----------------
     *      ' ~~         ~~ '
     *      ' ~~         ~~ '
     *      -----------------
     *      '(open to below)'
     *      -----------------
     */
    @Composable
    fun TopHalfTwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top)
        ) {
            TwoColumn(leftItems, rightItems)
        }
    }

    private enum class TwoColumnListSource { LEFT, RIGHT }



    @Composable
    fun OneColumn(
        items : List<MenuItem>,
        alignment: Alignment.Horizontal = Alignment.Start,
        fullWidth : Boolean = false
    ) {

        val columnContents : @Composable ColumnScope.() -> Unit = {

                val colItems = items.reduceUpdateOnClick { it() ; println("items") }.let {
                    MenuWindow.MenuWindowKnobListener.current.listenForKnob(
                        listData = items,

                        onItemClickAdapter = { it.onClicked() },
                        onSelectAdapter = { item, isNowSelected ->
                            when (item) {
                                is TextMenuItem -> item.copy(isSelected = isNowSelected)
                                is CheckBoxMenuItem -> item.copy(isSelected = isNowSelected)
                                is ImageMenuItem -> item.copy(isSelected = isNowSelected)
                                else -> error("Unsupported type")
                            }
                        },
                        isSelectableAdapter = { it.isSelectable }
                    ).value
                }
                for (item in colItems) {
                    item.toView(
                        chipOrientation = if (!item.isSelectable) {
                            ItemChipOrientation.NONE
                        } else {
                            if (alignment == Alignment.Start) ItemChipOrientation.W else ItemChipOrientation.E
                        },
                    )()
                }
            }


        Box (
            Modifier
                .background(ChipItemColors.MenuBackground)
                .fillMaxWidth()
        ){
            Row(Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = if (alignment == Alignment.Start) Arrangement.Start else Arrangement.End
            ) {
                if (fullWidth) {
                    Column(Modifier.weight(1F, true)) { columnContents() }
                } else {
                    Column(Modifier.weight(0.5f, true)) {
                        if (alignment == Alignment.Start) {
                            columnContents()
                        } else {
                            MenuItem.SPACER
                        }
                    }
                    Column(Modifier.weight(0.5f, true)) {
                        if (alignment == Alignment.End) {
                            columnContents()
                        } else {
                            MenuItem.SPACER
                        }
                    }
                }
            }
        }
    }

    @Composable
    internal fun TwoColumn(
        leftItems: List<MenuItem>,
        rightItems: List<MenuItem>
    ) {
        //We combine each list into a super-list
        //that behaves as one, so that scrolling
        //spans both columns

        val selectedItem = remember { mutableStateOf<ConjoinedListRecord<MenuItem, TwoColumnListSource>?>(null) }

        val circularList = mutableListOf<ConjoinedListRecord<MenuItem, TwoColumnListSource>>()

        if (leftItems.isNotEmpty()) {
            circularList.add(ConjoinedListRecord(leftItems[0], TwoColumnListSource.LEFT, 0))
        }

        circularList.addAll(
            rightItems.mapIndexed { index, menuItem ->
                ConjoinedListRecord(
                    menuItem,
                    TwoColumnListSource.RIGHT,
                    index
                )
            }
        )

        circularList.addAll(
            leftItems.drop(1).reversed()
                .map { ConjoinedListRecord(it, TwoColumnListSource.LEFT, leftItems.indexOf(it)) }
        )
        //We want this list to stay between compositions even though we mutate it.
        //If the remember isn't here, we infinitely recompose and I don't know why.
        val selectionOrderConjoinedList = remember (MenuWindow.MenuWindowKnobListener.current) { mutableStateOf(circularList) }

        val conjoinedList = MenuWindow.MenuWindowKnobListener.current.listenForKnob(
            listData = selectionOrderConjoinedList.value,

            onItemClickAdapter = {
                it.item.onClicked()
            },
            onSelectAdapter = { item, isNowSelected ->
                if (isNowSelected) {
                    selectedItem.value = item
                }
                println("WAT Currently selected: $item, $isNowSelected")
                ConjoinedListRecord(item.first.copyAndSetIsSelected(isNowSelected), item.second, item.third)
            },
            isSelectableAdapter = {
                it.item.isSelectable
            }
        ).value

        Box(
            Modifier
                .background(ChipItemColors.MenuBackground)
                .fillMaxWidth()
        ) {
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(Modifier.weight(0.5f, true)) {
                    conjoinedList
                        .filter { it.sourcePlacementEnum == TwoColumnListSource.LEFT }
                        .sortedBy { it.originalItemPosition }
                        .map { it.item }
                        .forEachIndexed { index, menuItem ->
                            menuItem.toView(
                                chipOrientation = if (!menuItem.isSelectable) {
                                    ItemChipOrientation.NONE
                                } else {
                                    when (index) {
                                        0 -> ItemChipOrientation.NW
                                        leftItems.lastIndex -> ItemChipOrientation.SW
                                        else -> ItemChipOrientation.W
                                    }
                                }
                            )()
                        }
                }
                Column(Modifier.weight(0.5f, true)) {
                    conjoinedList
                        .filter { it.sourcePlacementEnum == TwoColumnListSource.RIGHT }
                        .sortedBy { it.originalItemPosition }
                        .map { it.item }
                        .forEachIndexed { index, menuItem ->
                            menuItem.toView(
                                chipOrientation = if (!menuItem.isSelectable) {
                                    ItemChipOrientation.NONE
                                } else {
                                    when (index) {
                                        0 -> ItemChipOrientation.NE
                                        rightItems.lastIndex -> ItemChipOrientation.SE
                                        else -> ItemChipOrientation.E
                                    }
                                }
                            )()
                        }
                }
            }
        }
    }

}