package com.example.combinatorialpuzzle.ui1

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.math.sqrt
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    // JSON FILE
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
        // WIN MESSAGE
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

                if (level > 7) {
                    level = 1
                }

                won = false
            },

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 40.dp,
                    end = 40.dp,
                    bottom = 70.dp
                )
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
                .padding(
                    start = 40.dp,
                    end = 40.dp,
                    top = 70.dp
                )
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


    val root = JSONObject(text)


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
    // RETURN
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


    // =================================================
    // HIGHLIGHTED NODES
    // =================================================

    var highlightedNodes by remember(jsonFileName) {

        mutableStateOf<Set<Int>>(
            emptySet()
        )
    }


    // =================================================
    // HIGHLIGHTED EDGES
    // =================================================

    var highlightedEdges by remember(jsonFileName) {

        mutableStateOf<Set<Pair<Int, Int>>>(
            emptySet()
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

    val minX =
        graph.nodes.minOfOrNull {
            it.x
        } ?: 0f


    val maxX =
        graph.nodes.maxOfOrNull {
            it.x
        } ?: 1f


    val minY =
        graph.nodes.minOfOrNull {
            it.y
        } ?: 0f


    val maxY =
        graph.nodes.maxOfOrNull {
            it.y
        } ?: 1f


    // =================================================
    // TARGET COIN INDEX
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


        val graphWidth =
            maxX - minX


        val graphHeight =
            maxY - minY


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
            (canvasSize.width - padding * 2f) /
                    safeGraphWidth


        val scaleY =
            (canvasSize.height - padding * 2f) /
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
            (canvasSize.width - actualWidth) / 2f


        val offsetY =
            (canvasSize.height - actualHeight) / 2f


        return Offset(
            x =
                offsetX +
                        (node.x - minX) *
                        scale,

            y =
                offsetY +
                        (node.y - minY) *
                        scale
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
    // CHECK OCCUPIED
    // =================================================

    fun isOccupied(
        nodeId: Int,
        movingCoinIndex: Int
    ): Boolean {

        return coinNodes.withIndex().any { (index, coinNode) ->

            index != movingCoinIndex &&
                    coinNode == nodeId
        }
    }

    val coroutineScope = rememberCoroutineScope()


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
    // BLOCKED BY NEARBY COIN
    // =================================================

    fun isBlockedByNearbyCoin(
        destinationNode: Int,
        movingCoinIndex: Int
    ): Boolean {

        val nearbyNodes =
            getAdjacentNodes(
                destinationNode
            )


        return coinNodes.withIndex().any { (index, coinNode) ->

            index != movingCoinIndex &&
                    coinNode in nearbyNodes
        }
    }


    // =================================================
    // CHECK WHETHER NODE CAN BE ENTERED
    // =================================================

    fun canMoveTo(
        nodeId: Int,
        movingCoinIndex: Int
    ): Boolean {

        if (
            isOccupied(
                nodeId = nodeId,
                movingCoinIndex = movingCoinIndex
            )
        ) {
            return false
        }


        if (
            isBlockedByNearbyCoin(
                destinationNode = nodeId,
                movingCoinIndex = movingCoinIndex
            )
        ) {
            return false
        }


        return true
    }


    // =================================================
    // FIND ALL REACHABLE NODES
    // =================================================

    fun findAllReachableNodes(
        startNode: Int,
        movingCoinIndex: Int
    ): Pair<Set<Int>, Set<Pair<Int, Int>>> {

        val queue =
            ArrayDeque<Int>()


        val visited =
            mutableSetOf<Int>()


        queue.add(startNode)

        visited.add(startNode)


        // =================================================
        // BFS
        // =================================================

        while (queue.isNotEmpty()) {

            val currentNode =
                queue.removeFirst()


            val neighbors =
                getAdjacentNodes(
                    currentNode
                )


            for (nextNode in neighbors) {

                if (nextNode in visited) {
                    continue
                }


                if (
                    !canMoveTo(
                        nodeId = nextNode,
                        movingCoinIndex = movingCoinIndex
                    )
                ) {
                    continue
                }


                visited.add(nextNode)

                queue.add(nextNode)
            }
        }


        // =================================================
        // DESTINATION NODES
        // =================================================

        val reachableNodes =
            visited
                .filter {
                    it != startNode
                }
                .toSet()


        // =================================================
        // EDGES
        // =================================================

        val reachableEdges =
            graph.edges
                .filter { edge ->

                    edge.first in visited &&
                            edge.second in visited
                }
                .map {

                    Pair(
                        minOf(
                            it.first,
                            it.second
                        ),

                        maxOf(
                            it.first,
                            it.second
                        )
                    )
                }
                .toSet()


        return Pair(
            reachableNodes,
            reachableEdges
        )
    }


    // =================================================
    // FIND PATH
    // =================================================

    fun findPath(
        startNode: Int,
        destinationNode: Int,
        movingCoinIndex: Int
    ): List<Int> {

        val queue =
            ArrayDeque<Int>()


        val visited =
            mutableSetOf<Int>()


        val parent =
            mutableMapOf<Int, Int?>()


        queue.add(startNode)

        visited.add(startNode)

        parent[startNode] = null


        while (queue.isNotEmpty()) {

            val current =
                queue.removeFirst()


            if (current == destinationNode) {
                break
            }


            for (next in getAdjacentNodes(current)) {

                if (next in visited) {
                    continue
                }


                if (
                    !canMoveTo(
                        nodeId = next,
                        movingCoinIndex = movingCoinIndex
                    )
                ) {
                    continue
                }


                visited.add(next)

                parent[next] = current

                queue.add(next)
            }
        }


        // =================================================
        // NO PATH
        // =================================================

        if (destinationNode !in parent) {
            return emptyList()
        }


        // =================================================
        // BUILD PATH
        // =================================================

        val path =
            mutableListOf<Int>()


        var current: Int? =
            destinationNode


        while (current != null) {

            path.add(current)

            current = parent[current]
        }


        path.reverse()


        return path
    }


    // =================================================
    // MOVE COIN STEP BY STEP
    // =================================================

    suspend fun moveSelectedCoinToNode(
        destinationNode: Int
    ) {

        val coinIndex =
            selectedCoinIndex


        if (coinIndex == -1) {
            return
        }


        // =================================================
        // DESTINATION MUST BE GREEN
        // =================================================

        if (destinationNode !in highlightedNodes) {
            return
        }


        // =================================================
        // FIND PATH
        // =================================================

        val startNode =
            coinNodes[coinIndex]


        val path =
            findPath(
                startNode = startNode,
                destinationNode = destinationNode,
                movingCoinIndex = coinIndex
            )


        if (path.isEmpty()) {
            return
        }


        // =================================================
        // MOVE STEP BY STEP
        // =================================================

        for (node in path.drop(1)) {

            coinNodes =
                coinNodes
                    .toMutableList()
                    .also {
                        it[coinIndex] = node
                    }


            // You can increase this for slower animation.
            delay(300)
        }


        // =================================================
        // CLEAR SELECTION
        // =================================================

        selectedCoinIndex = -1

        highlightedNodes = emptySet()

        highlightedEdges = emptySet()
    }


    // =================================================
    // FIND GREEN NODE CLICKED
    // =================================================

    fun findClickedHighlightedNode(
        touchPosition: Offset
    ): Int? {

        for (nodeId in highlightedNodes) {

            val node =
                nodeMap[nodeId]
                    ?: continue


            val nodePosition =
                position(node)


            val dx =
                touchPosition.x -
                        nodePosition.x


            val dy =
                touchPosition.y -
                        nodePosition.y


            val distance =
                sqrt(
                    dx * dx +
                            dy * dy
                )


            if (distance < 35f) {
                return nodeId
            }
        }


        return null
    }


    // =================================================
    // SELECT COIN
    // =================================================

    fun selectCoinAt(
        touchPosition: Offset
    ) {

        graph.coins.forEachIndexed { index, _ ->

            val nodeId =
                coinNodes[index]


            val node =
                nodeMap[nodeId]
                    ?: return@forEachIndexed


            val coinPosition =
                position(node)


            val dx =
                touchPosition.x -
                        coinPosition.x


            val dy =
                touchPosition.y -
                        coinPosition.y


            val distance =
                sqrt(
                    dx * dx +
                            dy * dy
                )


            if (distance < 30f) {

                // =================================================
                // SELECT COIN
                // =================================================

                selectedCoinIndex = index


                // =================================================
                // FIND REACHABLE AREA
                // =================================================

                val result =
                    findAllReachableNodes(
                        startNode = nodeId,
                        movingCoinIndex = index
                    )


                highlightedNodes =
                    result.first


                highlightedEdges =
                    result.second


                return@forEachIndexed
            }
        }
    }


    // =================================================
    // GRAPH BOX
    // =================================================

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(R.drawable.game_background2),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        // =================================================
        // CANVAS
        // =================================================

        Canvas(

            modifier =
                Modifier
                    .size(
                        width = 600.dp,
                        height = 600.dp
                    )

                    .onSizeChanged {
                        canvasSize = it
                    }

                    .pointerInput(
                        jsonFileName,
                        canvasSize,
                        coinNodes,
                        selectedCoinIndex,
                        highlightedNodes
                    ) {

                        awaitEachGesture {

                            // =================================================
                            // FIRST TOUCH
                            // =================================================

                            val down =
                                awaitFirstDown(
                                    requireUnconsumed = false
                                )


                            val touchPosition =
                                down.position


                            // =================================================
                            // FIRST:
                            // CHECK GREEN NODE
                            // =================================================

                            if (
                                selectedCoinIndex != -1 &&
                                highlightedNodes.isNotEmpty()
                            ) {

                                val clickedNode =
                                    findClickedHighlightedNode(
                                        touchPosition
                                    )
                                if (clickedNode != null) {

                                    coroutineScope.launch {

                                        moveSelectedCoinToNode(
                                            destinationNode = clickedNode
                                        )
                                    }

                                    return@awaitEachGesture
                                }
                            }


                            // =================================================
                            // SECOND:
                            // CHECK COIN
                            // =================================================

                            selectCoinAt(
                                touchPosition
                            )
                        }
                    }

        ) {

            // =================================================
            // DRAWING VALUES
            // =================================================

            val padding =
                30f


            val graphWidth =
                maxX - minX


            val graphHeight =
                maxY - minY


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


            // =================================================
            // DRAW EDGES
            // =================================================

            graph.edges.forEach { edge ->

                val node1 =
                    nodeMap[edge.first]


                val node2 =
                    nodeMap[edge.second]


                if (
                    node1 != null &&
                    node2 != null
                ) {

                    val edgeKey =
                        Pair(
                            minOf(
                                edge.first,
                                edge.second
                            ),

                            maxOf(
                                edge.first,
                                edge.second
                            )
                        )


                    val start =
                        Offset(
                            x =
                                offsetX +
                                        (node1.x - minX) *
                                        scale,

                            y =
                                offsetY +
                                        (node1.y - minY) *
                                        scale
                        )


                    val end =
                        Offset(
                            x =
                                offsetX +
                                        (node2.x - minX) *
                                        scale,

                            y =
                                offsetY +
                                        (node2.y - minY) *
                                        scale
                        )


                    drawLine(

                        color =
                            if (
                                edgeKey in highlightedEdges
                            ) {
                                Color.Green
                            } else {
                                Color.Black
                            },

                        start = start,

                        end = end,

                        strokeWidth =
                            if (
                                edgeKey in highlightedEdges
                            ) {
                                6f
                            } else {
                                3f
                            }
                    )
                }
            }


            // =================================================
            // DRAW NODES
            // =================================================

            graph.nodes.forEach { node ->

                val nodePosition =
                    Offset(
                        x =
                            offsetX +
                                    (node.x - minX) *
                                    scale,

                        y =
                            offsetY +
                                    (node.y - minY) *
                                    scale
                    )


                drawCircle(

                    color =
                        when {

                            // =================================
                            // WIN NODE
                            // =================================

                            node.id == graph.winNode -> {
                                Color.Red
                            }


                            // =================================
                            // REACHABLE NODE
                            // =================================

                            node.id in highlightedNodes -> {
                                Color.Green
                            }


                            // =================================
                            // NORMAL NODE
                            // =================================

                            else -> {
                                Color.DarkGray
                            }
                        },

                    radius = 11f,

                    center = nodePosition
                )
            }


            // =================================================
            // DRAW COINS
            // =================================================

            graph.coins.forEachIndexed { coinIndex, coin ->

                val currentNodeId =
                    coinNodes[coinIndex]


                val node =
                    nodeMap[currentNodeId]


                if (node != null) {

                    val coinPosition =
                        Offset(
                            x =
                                offsetX +
                                        (node.x - minX) *
                                        scale,

                            y =
                                offsetY +
                                        (node.y - minY) *
                                        scale
                        )


                    // =============================================
                    // COIN COLOR
                    // =============================================

                    val coinColor =
                        if (
                            coin.id == graph.targetCoin
                        ) {

                            // Target coin
                            Color(0xFFFF6D00)

                        } else {

                            Color.Gray
                        }


                    // =============================================
                    // SELECTED COIN BORDER
                    // =============================================

                    if (
                        coinIndex == selectedCoinIndex
                    ) {

                        drawCircle(
                            color = Color.Blue,
                            radius = 18f,
                            center = coinPosition,
                            style = Stroke(4f)
                        )
                    }


                    // =============================================
                    // COIN
                    // =============================================

                    drawCircle(
                        color = coinColor,
                        radius = 13f,
                        center = coinPosition
                    )


                    // =============================================
                    // RED OUTER BORDER
                    // =============================================

                    drawCircle(
                        color = Color.Red,
                        radius = 15f,
                        center = coinPosition,
                        style = Stroke(3f)
                    )
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
        startLevel = 6
    )
}