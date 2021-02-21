package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.E

//A "Chip" is the little nubbin shown to indicate a scrollable item
//https://cdn.shopify.com/s/files/1/0366/7093/products/21c061cb-1bd7-4183-b72e-4a72a4b211a7_zpshihdw7rj.jpg?v=1571438673

enum class ItemChipOrientation{
    NONE,
    NW,
    NE,
    E,
    SE,
    SW,
    W
}

object ChipItemColors {
    val TEXT_WHITE = Color.White
    val TEXT_BLUE_LIGHT = Color.Blue
    val TEXT_BLUE_DARK = Color.Blue
    val TEXT_RED = Color.Red

    val MenuBackground = Color(48, 72, 107, 255)
}

@Composable
fun ScrollableSelectable(
    isSelected : Boolean = false,
    onSelectedTo : () -> Unit = {},
    onSelectedFrom : () -> Unit = {},
    onKnobClick : () -> Unit = {},
    content : @Composable () -> Unit
) {
    //This composable holds the state needed for the
    //circular linked list of selection + knob click events.
    content()
}

@Composable
fun EmptyMenuItem() {
    MenuItem()
}

@Composable
fun MenuItem(
    label : String = " ",
    labelColor : Color = ChipItemColors.TEXT_WHITE,
    chipOrientation: ItemChipOrientation = ItemChipOrientation.NONE,
    isSelected: Boolean = false
) {

    val chipWidth = 16.0F
    val chipColor = Color(121, 181, 220, 255)
    val chipHighlights = Color.White
    val highlightWidth = 4.0f

    val selected = remember(isSelected) {}

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 36.sp,
            modifier = Modifier
                .border(3.dp, Color.Red)
                .then(
                    when (chipOrientation) {
                        ItemChipOrientation.NW,
                        ItemChipOrientation.NE -> {
                            Modifier.padding(top = (chipWidth * 1.5).dp, bottom = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.SW,
                        ItemChipOrientation.SE -> {
                            Modifier.padding(bottom = (chipWidth * 1.5).dp, top = 5.dp, end = highlightWidth.dp, start = (chipWidth * 1.5).dp)
                        }
                        ItemChipOrientation.W -> {
                            Modifier.padding(start = (chipWidth * 1.5).dp, top = 5.dp, bottom = 5.dp)
                        }
                        else -> {
                            Modifier.padding(top = 5.dp, bottom = 5.dp, start = 25.dp)
                        }
                    }
                )
        )

        Canvas(modifier = Modifier.matchParentSize(), onDraw = {
            when (chipOrientation) {
                ItemChipOrientation.NW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, chipWidth),
                        end = Offset(this.size.height, chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(chipWidth, 0.0f),
                        end = Offset(chipWidth, this.size.height),
                        strokeWidth = 2 * chipWidth
                    )
                    //Line for top here
                }
                ItemChipOrientation.NE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, chipWidth),
                        end = Offset(this.size.width, chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - chipWidth, 0.0f),
                        end = Offset(this.size.width - chipWidth, this.size.height),
                        strokeWidth = 2 * chipWidth
                    )
                }
                ItemChipOrientation.E -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - chipWidth, 0.0f),
                        end = Offset(this.size.width - chipWidth, this.size.height - highlightWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (2 * chipWidth), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SE -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - this.size.height, this.size.height - chipWidth),
                        end = Offset(this.size.width, this.size.height - chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(this.size.width - chipWidth, 0.0f),
                        end = Offset(this.size.width - chipWidth, this.size.height),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(this.size.width - (2 * chipWidth), 0.0f),
                        end = Offset(this.size.width, 0.0f),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.SW -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(chipWidth, 0.0f),
                        end = Offset(chipWidth, this.size.height),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(0.0f, this.size.height - chipWidth),
                        end = Offset(this.size.height, this.size.height - chipWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth * 2, highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
                ItemChipOrientation.W -> {
                    this.drawLine(
                        brush = SolidColor(chipColor),
                        start = Offset(chipWidth, 0.0f),
                        end = Offset(chipWidth, this.size.height - highlightWidth),
                        strokeWidth = 2 * chipWidth
                    )
                    this.drawLine(
                        brush = SolidColor(chipHighlights),
                        start = Offset(0.0f, highlightWidth),
                        end = Offset(chipWidth * 2, highlightWidth),
                        strokeWidth = highlightWidth
                    )
                }
            }
        })

    }


}

@Composable
fun BmwChipMenu(
    contentLeft : @Composable () -> Unit,
    contentRight : @Composable () -> Unit
) {
    Box (Modifier
        .background(ChipItemColors.MenuBackground)
        .fillMaxWidth()
    ){
        Row(Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(Modifier.weight(0.5f, true)) {
                contentLeft()
            }
            Column(Modifier.weight(0.5f, true)) {
                contentRight()
            }
        }
    }
}