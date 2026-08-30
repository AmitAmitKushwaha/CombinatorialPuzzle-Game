package com.example.combinatorialpuzzle.ui1

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateListOf
import kotlin.math.sqrt

@Composable
fun GameScreen(
    onBackClick: () -> Unit
) {
    val bottom = remember {
        List(19) { i ->
            val n = i + 1

            Offset(
                n * 50f,
                650f
            )
        }
    }


    val top1 = remember {
        List(8) { i ->
            val n = i + 1
            Offset(
                n * 50f + 100f,
                520f
            )

        }
    }
    val top2 = remember {
        List(8) { i ->
            val n = i + 1

            Offset(550f + n * 50, 520f)

        }
    }
    val down1 = listOf(
        Offset(250f, 750f),
        Offset(250f, 850f),
    )
    val down2 = listOf(
        Offset(450f, 750f),
        Offset(450f, 850f),
    )

    val down3 = listOf(
        Offset(750f, 750f),
        Offset(750f, 850f),
    )

    val down4 = listOf(
        Offset(950f, 750f),
        Offset(950f, 850f),
    )


    // All coin positions
    val coinPositions = remember {
        mutableStateListOf(
            bottom[0],

            top1[0],
            top1[1],
            top1[2],
            top1[3],
            top1[4],
            top1[5],
            top1[6],
            top1[7],

            top2[0],
            top2[1],
            top2[2],
            top2[3],
            top2[4],
            top2[5],
            top2[6],
            top2[7],


        )
    }

// Which coin is currently being dragged
    var draggedCoinIndex by remember {
        mutableIntStateOf(-1)
    }

    var bottomCoin by remember {
        mutableStateOf(bottom[0])
    }

    // -------------------------
// Current position of coins
// -------------------------

    val bottomCoins = remember {
        mutableStateListOf(*bottom.toTypedArray())
    }


// -------------------------
// Drag information
// -------------------------

    var draggedGroup by remember {
        mutableStateOf("")
    }

    var isDragging by remember {
        mutableStateOf(false)
    }
    val adjacentNodes = mapOf(
        bottom[2] to listOf(top1[0], top1[1]),
        bottom[3] to listOf(top1[1]),
        bottom[4] to listOf(top1[2], top1[3], down1[0]),
        bottom[5] to listOf(top1[3]),
        bottom[6] to listOf(top1[4], top1[5]),
        bottom[7] to listOf(top1[5]),
        bottom[8] to listOf(top1[6], top1[7], down2[0]),
        bottom[9] to listOf(top1[7]),

        bottom[11] to listOf(top2[0]),
        bottom[12] to listOf(top2[0], top2[1]),
        bottom[13] to listOf(top2[2]),
        bottom[14] to listOf(top2[2], top2[3]),
        bottom[15] to listOf(top2[4]),
        bottom[16] to listOf(top2[4], top2[5]),
        bottom[17] to listOf(top2[6]),
        bottom[18] to listOf(top2[6], top2[7])
    )
    fun isOccupied(
        node: Offset,
        draggedIndex: Int
    ): Boolean {

        return coinPositions.indices.any { index ->

            if (index == draggedIndex) {
                false
            } else {
                (coinPositions[index] - node).getDistance() < 30f
            }
        }
    }


    val top1Coins = remember {
        mutableStateListOf(
            *top1.toTypedArray()
        )
    }

    val top2Coins = remember {
        mutableStateListOf(
            *top2.toTypedArray()
        )
    }

    // Coin's temporary position while dragging
    var dragPosition by remember {
        mutableStateOf<Offset?>(null)
    }

    val allNodes = remember {
        listOf(
            *bottom.toTypedArray(),
            *top1.toTypedArray(),
            *top2.toTypedArray(),
            *down1.toTypedArray(),
            *down2.toTypedArray(),
            *down3.toTypedArray(),
            *down4.toTypedArray()
        )
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(76.dp))
        Text(
            text = "Puzzle Game",
            style = MaterialTheme.typography.headlineLarge,

            )
        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = {
                onBackClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }


        Spacer(modifier = Modifier.height(26.dp))


        Canvas(
            modifier = Modifier
                .size(900.dp)
                .pointerInput(Unit) {

                    detectDragGestures(

                        onDragStart = { position ->

                            // Check TOP1
                            val top1Index = top1Coins.indexOfFirst { coinPosition ->
                                (position - coinPosition).getDistance() < 30f
                            }

                            if (top1Index != -1) {

                                draggedGroup = "top1"
                                draggedCoinIndex = top1Index
                                isDragging = true

                                dragPosition = top1Coins[top1Index]
                            }

                            else {

                                // Check TOP2
                                val top2Index = top2Coins.indexOfFirst { coinPosition ->
                                    (position - coinPosition).getDistance() < 30f
                                }

                                if (top2Index != -1) {

                                    draggedGroup = "top2"
                                    draggedCoinIndex = top2Index
                                    isDragging = true

                                    dragPosition = top2Coins[top2Index]
                                }

                                else {

                                    // Check BOTTOM
                                    val bottomIndex = bottomCoins.indexOfFirst { coinPosition ->
                                        (position - coinPosition).getDistance() < 30f
                                    }

                                    if (bottomIndex != -1) {

                                        draggedGroup = "bottom"
                                        draggedCoinIndex = bottomIndex
                                        isDragging = true

                                        dragPosition = bottomCoins[bottomIndex]
                                    }
                                }
                            }
                        },

                        onDrag = { change, dragAmount ->

                            if (isDragging && draggedCoinIndex != -1) {

                                change.consume()

                                val oldPosition = when (draggedGroup) {

                                    "top1" ->
                                        top1Coins[draggedCoinIndex]

                                    "top2" ->
                                        top2Coins[draggedCoinIndex]

                                    "bottom" ->
                                        bottomCoins[draggedCoinIndex]

                                    else ->
                                        return@detectDragGestures
                                }

                                val newPosition = Offset(
                                    x = oldPosition.x + dragAmount.x,
                                    y = oldPosition.y + dragAmount.y
                                )

                                when (draggedGroup) {

                                    "top1" ->
                                        top1Coins[draggedCoinIndex] = newPosition

                                    "top2" ->
                                        top2Coins[draggedCoinIndex] = newPosition

                                    "bottom" ->
                                        bottomCoins[draggedCoinIndex] = newPosition
                                }

                                dragPosition = newPosition
                            }
                        },

                        onDragEnd = {

                            if (draggedCoinIndex != -1) {

                                val currentPosition =
                                    coinPositions[draggedCoinIndex]

                                // Find nearest node
                                val nearestNode =
                                    allNodes.minByOrNull { node ->

                                        val dx =
                                            currentPosition.x - node.x

                                        val dy =
                                            currentPosition.y - node.y

                                        dx * dx + dy * dy
                                    }

                                // Put coin on nearest node
                                if (nearestNode != null) {

                                    coinPositions[draggedCoinIndex] =
                                        nearestNode
                                }
                            }

                            isDragging = false
                            draggedCoinIndex = -1
                        },

                        onDragCancel = {

                            isDragging = false
                            draggedCoinIndex = -1
                        }
                    )
                }


        ) {


            for (i in 0 until bottom.size - 1) {
                drawLine(Color.Black, start = bottom[i], end = bottom[i + 1], strokeWidth = 5f)
            }

            drawLine(Color.Black, start = bottom[2], top1[0], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[2], top1[1], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[3], top1[1], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[4], top1[2], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[4], top1[3], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[5], top1[3], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[6], top1[4], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[6], top1[5], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[7], top1[5], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[8], top1[6], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[8], top1[7], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[9], top1[7], strokeWidth = 5f)

            for (i in top1) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }


            drawLine(Color.Black, start = bottom[11], top2[0], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[12], top2[0], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[12], top2[1], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[13], top2[2], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[14], top2[2], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[14], top2[3], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[15], top2[4], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[16], top2[4], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[16], top2[5], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[17], top2[6], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[18], top2[6], strokeWidth = 5f)
            drawLine(Color.Black, start = bottom[18], top2[7], strokeWidth = 5f)

            for (i in top2) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }


            drawLine(Color.Black, start = bottom[4], end = down1[0], strokeWidth = 5f)
            drawLine(Color.Black, start = down1[1], end = down1[0], strokeWidth = 5f)

            for (i in down1) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }

            drawLine(Color.Black, start = bottom[8], end = down2[0], strokeWidth = 5f)
            drawLine(Color.Black, start = down2[1], end = down2[0], strokeWidth = 5f)

            for (i in down2) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }



            drawLine(Color.Black, start = bottom[14], end = down3[0], strokeWidth = 5f)
            drawLine(Color.Black, start = down3[1], end = down3[0], strokeWidth = 5f)

            for (i in down3) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }


            drawLine(Color.Black, start = bottom[18], end = down4[0], strokeWidth = 5f)
            drawLine(Color.Black, start = down4[1], end = down4[0], strokeWidth = 5f)

            for (i in down4) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }

            for (i in bottom) {
                drawCircle(Color.White, radius = 12f, center = i)
                drawCircle(Color.Red, radius = 12f, center = i, style = Stroke(5f))
            }

            drawCircle(Color.Green, radius = 12f, center = Offset(150f,650f), style = Stroke(5f))



















                   // -------------------------
                  // Draw all coins
                 // -------------------------

            for (index in coinPositions.indices) {

                val coinPosition = coinPositions[index]

                drawCircle(
                    color = Color(0xFFFFC107),
                    radius = 15f,
                    center = coinPosition
                )

                drawCircle(
                    color = Color(0xFFB8860B),
                    radius = 15f,
                    center = coinPosition,
                    style = Stroke(width = 7f)
                )
            }



        }//Canvas end
    }//Column end
}// GameScreen end

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen(
        onBackClick = {}
    )
}