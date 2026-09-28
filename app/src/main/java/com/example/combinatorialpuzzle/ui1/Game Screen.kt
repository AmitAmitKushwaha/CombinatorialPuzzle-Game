package com.example.combinatorialpuzzle.ui1

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import kotlin.math.sqrt
import androidx.compose.ui.res.imageResource
import com.example.combinatorialpuzzle.R


// =====================================================
// GAME SCREEN
// =====================================================

@Composable
fun GameScreen(
    onBackClick: () -> Unit,
    startLevel: Int = 1
) {

    // =================================================
    // LEVEL
    // =================================================

    var level by remember {
        mutableIntStateOf(startLevel)
    }


    // =================================================
    // WIN STATE
    // =================================================

    var won by remember {
        mutableStateOf(false)
    }


    // =================================================
    // JSON FILE FOR CURRENT LEVEL
    // =================================================

    val jsonFileName = "PuzzleGraph$level.json"


    // =================================================
    // SCREEN
    // =================================================

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // =================================================
        // GRAPH
        // =================================================

        GraphFromJson(
            context = LocalContext.current,
            jsonFileName = jsonFileName,
            onWin = {
                won = true
            }
        )

        // =================================================
        // WIN SCREEN
        // =================================================

        if (won) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 140.dp),

                contentAlignment = Alignment.TopCenter
            ) {

                Text(
                    text = "YOU WIN!",
                    color = Color.Red
                )
            }
        }


        // =================================================
        // LEVEL BUTTON
        // =================================================

        Button(
            onClick = {

                level++

                won = false

                if (level > 7) {
                    level = 1
                }
            },

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 70.dp)
        ) {

            Text(
                text = "Level $level"
            )
        }


        // =================================================
        // BACK BUTTON
        // =================================================

        Button(
            onClick = {
                onBackClick()
            },

            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 70.dp)
        ) {

            Text(
                text = "← Back"
            )
        }
    }
}






// =====================================================
// DATA CLASSES
// =====================================================

data class GraphNode(
    val id: Int,
    val x: Float,
    val y: Float
)


data class GraphCoin(
    val id: Int,
    val node: Int,
    val color: String
)


data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<Pair<Int, Int>>,
    val coins: List<GraphCoin>,
    val targetCoin: Int,
    val winNode: Int
)


// =====================================================
// LOAD GRAPH FROM JSON
// =====================================================

fun loadGraphFromJson(
    context: Context,
    fileName: String
): GraphData {

    val text =
        context.assets
            .open(fileName)
            .bufferedReader()
            .use {
                it.readText()
            }


    val root =
        JSONObject(text)


    // =================================================
    // NODES
    // =================================================

    val nodesJson =
        root.getJSONArray("nodes")

    val nodes =
        mutableListOf<GraphNode>()


    for (i in 0 until nodesJson.length()) {

        val n =
            nodesJson.getJSONObject(i)

        nodes.add(
            GraphNode(
                id = n.getInt("id"),
                x = n.getDouble("x").toFloat(),
                y = n.getDouble("y").toFloat()
            )
        )
    }


    // =================================================
    // EDGES
    // =================================================

    val edgesJson =
        root.getJSONArray("edges")

    val edges =
        mutableListOf<Pair<Int, Int>>()


    for (i in 0 until edgesJson.length()) {

        val e =
            edgesJson.getJSONArray(i)

        edges.add(
            Pair(
                e.getInt(0),
                e.getInt(1)
            )
        )
    }


    // =================================================
    // COINS
    // =================================================

    val coinsJson =
        root.getJSONArray("coins")

    val coins =
        mutableListOf<GraphCoin>()


    for (i in 0 until coinsJson.length()) {

        val c =
            coinsJson.getJSONObject(i)

        coins.add(
            GraphCoin(
                id = c.getInt("id"),
                node = c.getInt("node"),
                color = c.getString("color")
            )
        )
    }


    // =================================================
    // TARGET COIN
    // =================================================

    val targetCoin =
        root
            .getJSONObject("targetCoin")
            .getInt("id")


    // =================================================
    // WIN NODE
    // =================================================

    val winNode =
        root.getInt("winNode")


    // =================================================
    // RETURN GRAPH DATA
    // =================================================

    return GraphData(
        nodes = nodes,
        edges = edges,
        coins = coins,
        targetCoin = targetCoin,
        winNode = winNode
    )
}









// =====================================================
// GRAPH FROM JSON
// =====================================================

@Composable
fun GraphFromJson(
    context: Context,
    jsonFileName: String,
    onWin: () -> Unit
) {

    // =================================================
    // LOAD GRAPH
    // =================================================

    val graph =
        remember(jsonFileName) {

            loadGraphFromJson(
                context = context,
                fileName = jsonFileName
            )
        }


    // =================================================
    // NODE MAP
    // =================================================

    val nodeMap =
        remember(graph.nodes) {

            graph.nodes.associateBy {
                it.id
            }
        }


    // =================================================
    // COIN POSITIONS
    // =================================================

    var coinNodes by remember(jsonFileName) {

        mutableStateOf(
            graph.coins.map {
                it.node
            }
        )
    }


    // =================================================
    // SELECTED COIN
    // =================================================

    var selectedCoinIndex by remember(jsonFileName) {

        mutableIntStateOf(-1)
    }

    var highlightedNodes by remember(jsonFileName) {
        mutableStateOf<List<Int>>(emptyList())
    }


    // =================================================
    // DRAG START
    // =================================================

    var dragStart by remember(jsonFileName) {

        mutableStateOf(
            Offset.Zero
        )
    }


    // =================================================
    // DRAG END
    // =================================================

    var dragEnd by remember(jsonFileName) {

        mutableStateOf(
            Offset.Zero
        )
    }


    // =================================================
    // CANVAS SIZE
    // =================================================

    var canvasSize by remember(jsonFileName) {

        mutableStateOf(
            IntSize.Zero
        )
    }


    // =================================================
    // GRAPH BOUNDS
    // =================================================

    val minX = graph.nodes.minOfOrNull { it.x } ?: 0f


    val maxX = graph.nodes.maxOfOrNull { it.x } ?: 1f


    val minY = graph.nodes.minOfOrNull { it.y } ?: 0f


    val maxY = graph.nodes.maxOfOrNull { it.y } ?: 1f


    // =================================================
    // TARGET COIN
    // =================================================

    val targetCoinIndex =
        graph.coins.indexOfFirst {

            it.id == graph.targetCoin
        }


    // =================================================
    // WIN TRIGGER
    // =================================================

    var winTriggered by remember(jsonFileName) {

        mutableStateOf(false)
    }


    LaunchedEffect(
        coinNodes,
        graph.targetCoin,
        graph.winNode
    ) {

        if (
            !winTriggered &&
            targetCoinIndex != -1 &&
            coinNodes[targetCoinIndex] == graph.winNode
        ) {

            winTriggered = true

            onWin()
        }
    }

    // =================================================
    // POSITION FUNCTION
    // =================================================

    fun position(
        node: GraphNode
    ): Offset {

        if (canvasSize == IntSize.Zero) {
            return Offset.Zero
        }


        val padding = 30f


        val graphWidth = maxX - minX


        val graphHeight = maxY - minY


        val safeGraphWidth =
            if (graphWidth == 0f) {
                1f
            } else {
                graphWidth
            }


        val safeGraphHeight =
            if (graphHeight == 0f) {
                1f
            } else {
                graphHeight
            }


        val scaleX = (canvasSize.width - padding * 2f) / safeGraphWidth


        val scaleY = (canvasSize.height - padding * 2f) / safeGraphHeight


        val scale =
            minOf(scaleX, scaleY)


        val actualWidth = safeGraphWidth * scale


        val actualHeight = safeGraphHeight * scale


        val offsetX = (canvasSize.width - actualWidth) / 2f


        val offsetY = (canvasSize.height - actualHeight) / 2f


        return Offset(

            x =
                offsetX + (node.x - minX) * scale,

            y =
                offsetY + (node.y - minY) * scale
        )
    }


    // =================================================
    // CHECK ADJACENCY
    // =================================================

    fun isAdjacent(
        node1: Int,
        node2: Int
    ): Boolean {

        return graph.edges.any { edge ->

            (
                    edge.first == node1 &&
                            edge.second == node2
                    )
                    ||
                    (
                            edge.first == node2 &&
                                    edge.second == node1
                            )
        }
    }


    // =================================================
    // CHECK DESTINATION OCCUPIED
    // =================================================

    fun isOccupied(
        nodeId: Int,
        movingCoinIndex: Int
    ): Boolean {

        return coinNodes.withIndex().any {
                (index, coinNode) ->

            index != movingCoinIndex &&
                    coinNode == nodeId
        }
    }


    // =================================================
    // GET ADJACENT NODES
    // =================================================

    fun getAdjacentNodes(
        nodeId: Int
    ): List<Int> {

        return graph.edges

            .filter { edge ->

                edge.first == nodeId ||
                        edge.second == nodeId
            }

            .map { edge ->

                if (edge.first == nodeId) {
                    edge.second
                } else {
                    edge.first
                }
            }

            .distinct()
    }


    // =================================================
    // NEW RULE:
    //
    // DESTINATION IS BLOCKED IF ANOTHER COIN IS
    // ON ANY NODE ADJACENT TO THE DESTINATION.
    // =================================================

    fun isBlockedByNearbyCoin(
        destinationNode: Int,
        movingCoinIndex: Int
    ): Boolean {

        // Find nodes adjacent to destination
        val nearbyNodes =
            getAdjacentNodes(
                destinationNode
            )


        // Check whether another coin is
        // on one of those nearby nodes
        return coinNodes.withIndex().any {
                (index, coinNode) ->

            index != movingCoinIndex &&
                    coinNode in nearbyNodes
        }
    }


    // =================================================
    // GRAPH BOX
    // =================================================

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {


        // =================================================
        // CANVAS
        // =================================================

        Canvas(

            modifier = Modifier

                .size(
                    width = 600.dp,
                    height = 600.dp
                )

                .background(
                    Color.White
                )

                .onSizeChanged {

                    canvasSize = it
                }

                // =================================================
                // GESTURE
                // =================================================

                .pointerInput(
                    jsonFileName,
                    canvasSize,
                    coinNodes
                ) {

                    detectDragGestures(

                        // =========================================
                        // DRAG START
                        // =========================================
                        onDragStart = { touchPosition ->

                            selectedCoinIndex = -1
                            highlightedNodes = emptyList()

                            dragStart = touchPosition
                            dragEnd = touchPosition

                            // Find touched coin
                            graph.coins.forEachIndexed { index, _ ->

                                val nodeId = coinNodes[index]

                                val node = nodeMap[nodeId]

                                if (node != null) {

                                    val coinPosition = position(node)

                                    val dx =
                                        touchPosition.x - coinPosition.x

                                    val dy =
                                        touchPosition.y - coinPosition.y

                                    val distance =
                                        sqrt(
                                            dx * dx +
                                                    dy * dy
                                        )

                                    if (distance < 70f) {

                                        selectedCoinIndex = index

                                        // =========================================
                                        // FIND VALID GREEN NODES
                                        // =========================================

                                        val currentNode = coinNodes[index]

                                        val validNodes =
                                            getAdjacentNodes(currentNode)
                                                .filter { destinationNode ->

                                                    // Destination must be empty
                                                    !isOccupied(
                                                        destinationNode,
                                                        index
                                                    )

                                                    // Destination must not be blocked
                                                }
                                                .filter { destinationNode ->

                                                    !isBlockedByNearbyCoin(
                                                        destinationNode,
                                                        index
                                                    )
                                                }

                                        highlightedNodes = validNodes
                                    }
                                }
                            }
                        },


                        // =========================================
                        // DRAG
                        // =========================================

                        onDrag = {
                                change, _ ->

                            dragEnd =
                                change.position

                            change.consume()
                        },

                        // =========================================
                        // DRAG END
                        // =========================================

                        onDragEnd = {

                            val coinIndex =
                                selectedCoinIndex


                            if (coinIndex != -1) {

                                val currentNode =
                                    coinNodes[coinIndex]


                                // =================================
                                // DRAG DIRECTION
                                // =================================

                                val dx =
                                    dragEnd.x - dragStart.x


                                val dy =
                                    dragEnd.y - dragStart.y


                                val dragDistance =
                                    sqrt(
                                        dx * dx + dy * dy
                                    )


                                if (dragDistance > 20f) {

                                    val dragUnitX =
                                        dx /
                                                dragDistance


                                    val dragUnitY =
                                        dy /
                                                dragDistance


                                    // =================================
                                    // FIND ADJACENT NODES
                                    // =================================

                                    val adjacentNodes =
                                        getAdjacentNodes(
                                            currentNode
                                        )


                                    // =================================
                                    // FIND BEST DESTINATION
                                    // =================================

                                    var bestNode: Int? = null


                                    var bestScore = -Float.MAX_VALUE


                                    for (
                                    adjacentNode
                                    in adjacentNodes
                                    ) {


                                        // ---------------------------------
                                        // MUST BE ADJACENT
                                        // ---------------------------------

                                        if (
                                            !isAdjacent(
                                                currentNode,
                                                adjacentNode
                                            )
                                        ) {
                                            continue
                                        }


                                        // ---------------------------------
                                        // RULE 1:
                                        // DESTINATION ITSELF HAS A COIN
                                        // ---------------------------------

                                        if (
                                            isOccupied(
                                                adjacentNode,
                                                coinIndex
                                            )
                                        ) {

                                            continue
                                        }


                                        // ---------------------------------
                                        // RULE 2:
                                        //
                                        // A COIN IS ON A NODE ADJACENT
                                        // TO THE DESTINATION
                                        // ---------------------------------

                                        if (
                                            isBlockedByNearbyCoin(
                                                adjacentNode,
                                                coinIndex
                                            )
                                        ) {
                                            continue
                                        }


                                        // ---------------------------------
                                        // GET POSITIONS
                                        // ---------------------------------

                                        val currentGraphNode =
                                            nodeMap[currentNode]


                                        val nextGraphNode =
                                            nodeMap[adjacentNode]


                                        if (
                                            currentGraphNode != null &&
                                            nextGraphNode != null
                                        ) {

                                            val currentPosition =
                                                position(
                                                    currentGraphNode
                                                )


                                            val nextPosition =
                                                position(
                                                    nextGraphNode
                                                )


                                            // ---------------------------------
                                            // DIRECTION
                                            // ---------------------------------

                                            val nodeDx =
                                                nextPosition.x - currentPosition.x


                                            val nodeDy =
                                                nextPosition.y - currentPosition.y


                                            val nodeDistance =
                                                sqrt(nodeDx * nodeDx + nodeDy * nodeDy)


                                            if (
                                                nodeDistance > 0f
                                            ) {

                                                val nodeUnitX =
                                                    nodeDx /
                                                            nodeDistance


                                                val nodeUnitY =
                                                    nodeDy /
                                                            nodeDistance


                                                // ---------------------------------
                                                // DOT PRODUCT
                                                // ---------------------------------

                                                val score =
                                                    dragUnitX * nodeUnitX + dragUnitY * nodeUnitY


                                                if (
                                                    score >
                                                    bestScore
                                                ) {

                                                    bestScore =
                                                        score

                                                    bestNode =
                                                        adjacentNode
                                                }
                                            }
                                        }
                                    }


                                    // =================================
                                    // MOVE COIN
                                    // =================================

                                    bestNode?.let {

                                            destination ->

                                        if (
                                            bestScore > 0.5f
                                        ) {

                                            coinNodes =
                                                coinNodes
                                                    .toMutableList()
                                                    .also {

                                                        it[coinIndex] =
                                                            destination
                                                    }
                                        }
                                    }
                                }

                                selectedCoinIndex = -1
                                highlightedNodes = emptyList()
                            }
                        },


                        // =========================================
                        // DRAG CANCEL
                        // =========================================

                        onDragCancel = {
                            selectedCoinIndex = -1
                            highlightedNodes = emptyList()
                        }
                    )
                }

        ) {

            // =====================================================
            // DRAWING
            // =====================================================

            val padding = 30f


            val graphWidth = maxX - minX


            val graphHeight = maxY - minY


            val safeGraphWidth =
                if (graphWidth == 0f) {
                    1f
                } else {
                    graphWidth
                }


            val safeGraphHeight =
                if (graphHeight == 0f) {
                    1f
                } else {
                    graphHeight
                }


            val scaleX =
                (size.width - padding * 2f) /
                        safeGraphWidth


            val scaleY =
                (size.height - padding * 2f) /
                        safeGraphHeight


            val scale =
                minOf(
                    scaleX,
                    scaleY
                )


            val actualWidth =
                safeGraphWidth * scale


            val actualHeight =
                safeGraphHeight * scale


            val offsetX =
                (size.width - actualWidth) / 2f


            val offsetY =
                (size.height - actualHeight) / 2f


            // =====================================================
            // DRAW EDGES
            // =====================================================

            graph.edges.forEach { edge ->

                val node1 =
                    nodeMap[edge.first]

                val node2 =
                    nodeMap[edge.second]


                if (
                    node1 != null &&
                    node2 != null
                ) {

                    drawLine(

                        color = Color.Black,

                        start = Offset(

                            x =
                                offsetX + (node1.x - minX) * scale,

                            y =
                                offsetY + (node1.y - minY) * scale
                        ),

                        end = Offset(

                            x =
                                offsetX + (node2.x - minX) * scale,

                            y =
                                offsetY + (node2.y - minY) * scale
                        ),

                        strokeWidth = 3f
                    )
                }
            }

            // ================================================
            // DRAW NODES
            // ================================================


// ================================================
// DRAW NODES
// ================================================

            graph.nodes.forEach { node ->

                drawCircle(
                    color =
                        when {

                            // WIN NODE
                            node.id == graph.winNode -> {
                                Color.Red
                            }

                            // VALID MOVE NODE
                            node.id in highlightedNodes -> {
                                Color.Green
                            }

                            // NORMAL NODE
                            else -> {
                                Color.DarkGray
                            }
                        },

                    radius = 11f,

                    center = Offset(
                        x = offsetX + (node.x - minX) * scale,
                        y = offsetY + (node.y - minY) * scale
                    )
                )
            }
            // =====================================================
            // DRAW COINS
            // =====================================================

            graph.coins.forEachIndexed {

                    coinIndex,
                    coin ->

                val currentNodeId =
                    coinNodes[coinIndex]


                val node =
                    nodeMap[currentNodeId]


                if (node != null) {



                    // =============================================
                    // COIN COLOR
                    // =============================================
                    val coinColor =
                        if (coin.id == graph.targetCoin) {
                            Color.Green    // Golden target coin
                        } else {
                            Color.Gray    // All other coins
                        }



                    // =============================================
                    // COIN POSITION
                    // =============================================

                    val coinPosition =
                        Offset(

                            x =
                                offsetX + (node.x - minX) * scale,

                            y =
                                offsetY + (node.y - minY) * scale
                        )


                    // =============================================
                    // COIN
                    // =============================================

                    drawCircle(color = coinColor, radius = 11f, center = coinPosition)


                    // =============================================
                    // RED BORDER
                    // =============================================

                    drawCircle(color = Color.Red,radius = 12f,center = coinPosition,style = Stroke(5f))
                }
            }
        }
    }
}

// =====================================================
// PREVIEW
// =====================================================

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 700
)
@Composable
fun GameScreenPreview() {

    GameScreen(
        onBackClick = {},
        startLevel = 5

    )
}