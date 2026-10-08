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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
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
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat

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




    var undoRequest by remember {
        mutableIntStateOf(0)
    }

    // =================================================
    // WIN STATE
    // =================================================

    var won by remember {
        mutableStateOf(false)
    }


    var showInstruction by remember { mutableStateOf(level==1) }
    var displayedInstruction by remember { mutableStateOf("") }

    val instructionText =
        "👆 Drag the log/apple to a connected adjacent node.\n" +
                "🚫 A move is blocked if another log/apple is next to the destination node.\n" +
                "🎯 Reach the basket to complete the level!"


    LaunchedEffect(level) {

        if (level != 1) {
            showInstruction = false
            return@LaunchedEffect
        }

        showInstruction = true
        displayedInstruction = ""

        // Show words one by one
        for (word in instructionText.split(" ")) {
            displayedInstruction =
                if (displayedInstruction.isEmpty()) {
                    word
                } else {
                    "$displayedInstruction $word"
                }

            delay(200.milliseconds)   // speed of words appearing
        }

        // Keep complete instruction visible
        delay(2100.milliseconds)

        // Disappear
        showInstruction = false
    }


    // =================================================
    // JSON FILE
    // =================================================

    val jsonFileName = "PuzzleGraph$level.json"


    // =================================================
    // SCREEN
    // =================================================
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {


        // ==========================================
        // FULL SCREEN BACKGROUND
        // ==========================================

        Image(
            painter = painterResource(
                R.drawable.game_background3
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        // =================================================
        // GRAPH
        // =================================================

        GraphFromJson(
            context = LocalContext.current,
            jsonFileName = jsonFileName,
            undoRequest = undoRequest,
            onWin = {
                won = true
            },
            onUndo = {
                won = false
            }
        )


            // =================================================
            // LEVEL BUTTON
            // =================================================

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        start = 12.dp,
                        bottom = 80.dp
                    )
                    .size(
                        width = 160.dp,
                        height = 80.dp
                    )
                    .clickable {


                        // After level 7, return to level 1
                        if (level < 8) {
                            level++
                        }

                        // Reset win state
                        won = false
                    },
                contentAlignment = Alignment.Center
            ) {

                // Wooden level PNG
                Image(
                    painter = painterResource(
                        R.drawable.level
                    ),
                    contentDescription = "Level button",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Level text
                Text(
                    text = " 🧩Level $level",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            // =================================================
            // WIN MESSAGE
            // =================================================

            if (won) {


                Text(
                    text = "YOU WIN",
                    color = Color.Red,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = 120.dp
                        )
                )
            }


            // =================================================
            // BACK BUTTON
            // =================================================

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 10.dp,
                        bottom = 80.dp
                    )
                    .size(
                        width = 160.dp,
                        height = 80.dp
                    )
                    .clickable {
                            if (level > 1) {
                                level--
                            } else {
                                onBackClick()
                            }
                    },
                contentAlignment = Alignment.Center
            ) {

                // Wooden level PNG
                Image(
                    painter = painterResource(
                        R.drawable.level
                    ),
                    contentDescription = "Level button",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Back",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

            }


            // =================================================
            // UNDO BUTTON
            // =================================================

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 120.dp,
                        bottom = 160.dp
                    )
                    .size(
                        width = 160.dp,
                        height = 80.dp
                    )
                    .clickable {
                        undoRequest++
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.level),
                    contentDescription = "Undo button",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Undo",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

            }



        // =================================================
        //  //instruction
        // =================================================


            if (showInstruction) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            start = 1.dp,
                            end = 1.dp,
                            top = 55.dp
                        )
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {


                    // Background instruction image
                    Image(
                        painter = painterResource(id = R.drawable.instruction),
                        contentDescription = "How to Play",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                    )


                    Text(
                        text = displayedInstruction,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 45.dp,
                                end = 45.dp,
                                top = 55.dp,
                                bottom = 25.dp
                            )
                    )
                }
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

    val nodesJson = root.getJSONArray("nodes")

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
    undoRequest: Int,
    onWin: () -> Unit,
    onUndo: () -> Unit
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


    var previousCoinNodes by remember(jsonFileName) {
        mutableStateOf<List<Int>?>(null)
    }

    // =================================================
    // SELECTED COIN
    // =================================================

    var selectedCoinIndex by remember(jsonFileName) {

        mutableIntStateOf(-1)
    }


    var blockedCoinIndices by remember(jsonFileName) {
        mutableStateOf<Set<Int>>(emptySet())
    }

    // =================================================
    // HIGHLIGHTED NODES
    // =================================================

    var highlightedNodes by remember(jsonFileName) {

        mutableStateOf<Set<Int>>(
            emptySet()
        )
    }
    var targetPosition by remember(jsonFileName) {
        mutableStateOf(Offset.Zero)
    }

    var isMoving by remember(jsonFileName) {
        mutableStateOf(false)
    }

    val animatedPosition by animateOffsetAsState(
        targetValue = targetPosition,
        animationSpec = tween(250),
        label = "coinMovement"
    )

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

    LaunchedEffect(undoRequest) {

        if (undoRequest > 0 && previousCoinNodes != null && !isMoving) {

            coinNodes = previousCoinNodes!!

            selectedCoinIndex = -1
            highlightedNodes = emptySet()
            blockedCoinIndices = emptySet()

            winTriggered = false
            isMoving = false

            onUndo()

            previousCoinNodes = null
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // =================================================
    // APPLE IMAGE + ANIMATION
    // =================================================

    val appleImage = ImageBitmap.imageResource(
        R.drawable.apple
    )

    val logImage = ImageBitmap.imageResource(
        R.drawable.log
    )

    val basketImage = ImageBitmap.imageResource(
        R.drawable.basket
    )

    val greenNodeImage =
        ImageBitmap.imageResource(R.drawable.green_node)

    val blackNodeImage =
        ImageBitmap.imageResource(R.drawable.black_node)



    val coinScale by animateFloatAsState(
        targetValue =
            if (selectedCoinIndex != -1) 1.15f else 1f,
        animationSpec = tween(
            durationMillis = 200),
        label = "coinScale"
    )


    // =================================================
    // RED ALERT FUNCTION
    // =================================================


    val redTransition = rememberInfiniteTransition(
        label = "blockedCoinAnimation"
    )

    val redAlpha by redTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 450
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "redAlpha"
    )

    val redBorderWidth by redTransition.animateFloat(
        initialValue = 2f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 450
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "redBorderWidth"
    )


    // =================================================
    // GREEN ALERT FUNCTION
    // =================================================

    val greenTransition = rememberInfiniteTransition(
        label = "greenNodeAnimation"
    )

    val greenAlpha by greenTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "greenAlpha"
    )

    val greenScale by greenTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "greenScale"
    )


    // =================================================
// BASKET FLOAT ANIMATION
// =================================================

    val basketTransition = rememberInfiniteTransition(
        label = "basketAnimation"
    )

    val basketFloat by basketTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "basketFloat"
    )

    val basketScale by basketTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "basketScale"
    )



    // =================================================
    // GLITTER FUNCTION
    // =================================================


    val infiniteTransition = rememberInfiniteTransition(
        label = "glitterAnimation"
    )

    val glitterAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitterAlpha"
    )

    val glitterScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitterScale"
    )

    // =================================================
    // POSITION FUNCTION
    // =================================================

    fun position(
        node: GraphNode
    ): Offset {

        if (canvasSize == IntSize.Zero) {
            return Offset.Zero
        }


        val padding = 50f


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



    // find blocked coin


    fun findBlockingCoins(
        selectedCoinIndex: Int
    ): Set<Int> {

        if (selectedCoinIndex == -1) {
            return emptySet()
        }

        val selectedNode =
            coinNodes[selectedCoinIndex]

        val blockingCoins =
            mutableSetOf<Int>()



        // Check every node directly reachable from
        // the selected coin.


        val adjacentNodes =
            getAdjacentNodes(selectedNode)

        for (destinationNode in adjacentNodes) {

            // A coin on the destination itself blocks movement.
            graph.coins.forEachIndexed { coinIndex, _ ->

                if (coinIndex == selectedCoinIndex) {
                    return@forEachIndexed
                }

                val otherCoinNode =
                    coinNodes[coinIndex]

                // Coin is sitting on the destination.
                if (otherCoinNode == destinationNode) {

                    blockingCoins.add(coinIndex)
                }

                // Coin is adjacent to the destination.
                val nodesAroundDestination =
                    getAdjacentNodes(destinationNode)

                if (
                    otherCoinNode in nodesAroundDestination
                ) {

                    blockingCoins.add(coinIndex)
                }
            }
        }

        return blockingCoins
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

        // Start animation only after a valid path is found.
        isMoving = true

        previousCoinNodes = coinNodes

        // =================================================
        // MOVE STEP BY STEP
        // =================================================
        for (node in path.drop(1)) {

            // Move animation target to the next node
            targetPosition = position(
                graph.nodes.first { it.id == node }
            )

            // Wait for animation to finish
            delay(150.milliseconds)

            // Now update the actual coin node
            coinNodes = coinNodes.toMutableList().also {
                it[coinIndex] = node
            }
        }


        // =================================================
        // CLEAR SELECTION
        // =================================================

        selectedCoinIndex = -1

        highlightedNodes = emptySet()

        blockedCoinIndices = emptySet()

        isMoving = false

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

        // Do not select another coin while a coin is moving.
        if (isMoving) {
            return
        }
        graph.coins.forEachIndexed { index, _ ->

            val nodeId = coinNodes[index]

            val node = nodeMap[nodeId]
                ?: return@forEachIndexed

            val coinPosition = position(node)

            val dx = touchPosition.x - coinPosition.x
            val dy = touchPosition.y - coinPosition.y

            val distance = sqrt(
                dx * dx + dy * dy
            )

            if (distance < 50f) {

                // ==========================================
                // SELECT COIN
                // ==========================================

                selectedCoinIndex = index

                // Prevent animation from jumping to (0,0)
                targetPosition = coinPosition

                isMoving = false

                // Remove previous red borders
                blockedCoinIndices = emptySet()

                // ==========================================
                // FIND VALID DESTINATIONS
                // ==========================================

                val result = findAllReachableNodes(
                    startNode = nodeId,
                    movingCoinIndex = index
                )

                highlightedNodes = result.first

                // ==========================================
                // COIN CANNOT MOVE
                // ==========================================

                if (highlightedNodes.isEmpty()) {

                    blockedCoinIndices =
                        findBlockingCoins(index)

                } else {

                    blockedCoinIndices =
                        emptySet()
                }

                return@forEachIndexed
            }

        }


    }

    LaunchedEffect(undoRequest) {

        if (undoRequest > 0 && previousCoinNodes != null && !isMoving) {

            coinNodes = previousCoinNodes!!

            selectedCoinIndex = -1
            highlightedNodes = emptySet()
            blockedCoinIndices = emptySet()

            winTriggered = false
            isMoving = false

            previousCoinNodes = null
        }
    }

    // =================================================
    // GRAPH BOX
    // =================================================

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        contentAlignment = Alignment.Center
    ) {

        // ==============================================
        // GRAPH BACKGROUND
        // ==============================================

        // Calculate graph dimensions
        val density = LocalDensity.current

        val graphWidth = maxX - minX
        val graphHeight = maxY - minY

        val safeGraphWidth =
            if (graphWidth == 0f) 1f else graphWidth

        val safeGraphHeight =
            if (graphHeight == 0f) 1f else graphHeight

        val scaleX =
            (canvasSize.width - 100f) / safeGraphWidth

        val scaleY =
            (canvasSize.height - 100f) / safeGraphHeight

        val scale =
            minOf(scaleX, scaleY)

        val actualWidth =
            safeGraphWidth * scale + 100f

        val actualHeight =
            safeGraphHeight * scale + 100f

        val imageWidthDp =
            with(density) {
                actualWidth.toDp()
            }

        val imageHeightDp =
            with(density) {
                actualHeight.toDp()
            }

        // GRAPH BACKGROUND
        Image(
            painter = painterResource(
                R.drawable.leaf_game6
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )



        // ==============================================
        // GRAPH CANVAS
        // ==============================================

        Canvas(
            modifier = Modifier
                .fillMaxSize()
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

                        val down = awaitFirstDown(
                            requireUnconsumed = false
                        )

                        val touchPosition = down.position


                        // ==================================
                        // CLICK GREEN NODE
                        // ==================================

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


                        // ==================================
                        // SELECT COIN
                        // ==================================

                        selectCoinAt(
                            touchPosition
                        )
                    }
                }
        ) {

            // ==========================================
            // KEEP YOUR EXISTING CANVAS DRAWING CODE HERE
            // ==========================================

            val padding = 50f

            val graphWidth = maxX - minX
            val graphHeight = maxY - minY

            val safeGraphWidth =
                if (graphWidth == 0f) 1f else graphWidth

            val safeGraphHeight =
                if (graphHeight == 0f) 1f else graphHeight

            val scaleX =
                (size.width - padding * 2f) /
                        safeGraphWidth

            val scaleY =
                (size.height - padding * 2f) /
                        safeGraphHeight

            val scale =
                minOf(scaleX, scaleY)

            val actualWidth =
                safeGraphWidth * scale

            val actualHeight =
                safeGraphHeight * scale

            val offsetX =
                (size.width - actualWidth) / 2f

            val offsetY =
                (size.height - actualHeight) / 2f


            // ==========================================
            // DRAW EDGES
            // ==========================================

            graph.edges.forEach { edge ->

                val node1 = nodeMap[edge.first]
                val node2 = nodeMap[edge.second]

                if (
                    node1 != null &&
                    node2 != null
                ) {

                    val start = Offset(
                        x = offsetX +
                                (node1.x - minX) * scale,

                        y = offsetY +
                                (node1.y - minY) * scale
                    )

                    val end = Offset(
                        x = offsetX +
                                (node2.x - minX) * scale,

                        y = offsetY +
                                (node2.y - minY) * scale
                    )

                    drawLine(
                        color = Color.Black,
                        start = start,
                        end = end,
                        strokeWidth = 5f
                    )
                }
            }




            // ==========================================
            // DRAW NODES
            // ==========================================
            graph.nodes.forEach { node ->

                val nodePosition = Offset(
                    x = offsetX +
                            (node.x - minX) * scale,

                    y = offsetY +
                            (node.y - minY) * scale
                )

                val isWinNode =
                    node.id == graph.winNode

                if (isWinNode) {

                    // Is the basket currently reachable?
                    val isBasketReachable =
                        node.id in highlightedNodes

                    val basketSize =
                        if (isBasketReachable) {
                            60f * basketScale
                        } else {
                            60f
                        }

                    val basketY =
                        if (isBasketReachable) {
                            nodePosition.y + basketFloat
                        } else {
                            nodePosition.y
                        }

                    // Golden glow when basket is reachable
                    if (isBasketReachable) {
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(
                                alpha = 0.30f
                            ),
                            radius = basketSize / 2f + 12f,
                            center = Offset(
                                nodePosition.x,
                                basketY
                            )
                        )
                    }

                    drawImage(
                        image = basketImage,

                        dstOffset = IntOffset(
                            x = (
                                    nodePosition.x -
                                            basketSize / 2f
                                    ).toInt(),

                            y = (
                                    basketY -
                                            basketSize / 2f
                                    ).toInt()
                        ),

                        dstSize = IntSize(
                            width = basketSize.toInt(),
                            height = basketSize.toInt()
                        )
                    )
                }

                else {

                    val isHighlighted =
                        node.id in highlightedNodes

                    val baseNodeSize = 65f

                    val nodeSize =
                        if (isHighlighted) {
                            baseNodeSize * greenScale
                        } else {
                            baseNodeSize
                        }

                    val nodeImage =
                        if (isHighlighted) {
                            greenNodeImage
                        } else {
                            blackNodeImage
                        }

                    // Green glow
                    if (isHighlighted) {

                        drawCircle(
                            color = Color.Green.copy(
                                alpha = greenAlpha * 0.30f
                            ),
                            radius = nodeSize / 2f + 8f,
                            center = nodePosition
                        )
                    }

                    drawImage(
                        image = nodeImage,

                        dstOffset = IntOffset(
                            x = (
                                    nodePosition.x -
                                            nodeSize / 2f
                                    ).toInt(),

                            y = (
                                    nodePosition.y -
                                            nodeSize / 2f
                                    ).toInt()
                        ),

                        dstSize = IntSize(
                            width = nodeSize.toInt(),
                            height = nodeSize.toInt()
                        )
                    )
                }
            }








            // ==========================================
            // DRAW COINS
            // ==========================================

            graph.coins.forEachIndexed { coinIndex, coin ->

                val currentNodeId =
                    coinNodes[coinIndex]

                val node =
                    nodeMap[currentNodeId]

                if (node != null) {

                    val normalPosition = Offset(
                        x = offsetX + (node.x - minX) * scale,
                        y = offsetY + (node.y - minY) * scale
                    )

                    val coinPosition =
                        if (coinIndex == selectedCoinIndex && isMoving) {
                            animatedPosition
                        } else {
                            normalPosition
                        }

                    val isSelected =
                        coinIndex == selectedCoinIndex

                    val isTargetCoin =
                        coin.id == graph.targetCoin

                    val coinSize =
                        if (isSelected) 65f else 70f

                    val coinImage =
                        if (isTargetCoin) {
                            appleImage
                        } else {
                            logImage
                        }



                    // ==========================================
                    // RED BORDER FOR BLOCKING COIN
                     // ==========================================

                    if (coinIndex in blockedCoinIndices) {

                        // Pulsing red outer glow
                        drawCircle(
                            color = Color.Red.copy(
                                alpha = redAlpha * 0.25f
                            ),
                            radius = coinSize / 2f + 12f,
                            center = coinPosition
                        )

                        // Pulsing red border
                        drawCircle(
                            color = Color.Red.copy(
                                alpha = redAlpha
                            ),
                            radius = coinSize / 2f + 7f,
                            center = coinPosition,
                            style = Stroke(
                                width = redBorderWidth
                            )
                        )
                    }


                    // GOLDEN GLOW
                    if (isSelected) {

                        // ==========================================
                        // GOLDEN GLOW
                        // ==========================================

                        drawCircle(
                            color = Color(0xFFFFD54F)
                                .copy(alpha = glitterAlpha * 0.35f),
                            radius = 45f * glitterScale,
                            center = coinPosition
                        )

                        drawCircle(
                            color = Color(0xFFFFC107)
                                .copy(alpha = glitterAlpha),
                            radius = 38f,
                            center = coinPosition,
                            style = Stroke(
                                width = 3f
                            )
                        )


                        // ==========================================
                        // GLITTER SPARKLES
                        // ==========================================

                        val sparklePositions = listOf(
                            Offset(
                                coinPosition.x,
                                coinPosition.y - 45f
                            ),

                            Offset(
                                coinPosition.x + 42f,
                                coinPosition.y - 25f
                            ),

                            Offset(
                                coinPosition.x + 42f,
                                coinPosition.y + 25f
                            ),

                            Offset(
                                coinPosition.x,
                                coinPosition.y + 45f
                            ),

                            Offset(
                                coinPosition.x - 42f,
                                coinPosition.y + 25f
                            ),

                            Offset(
                                coinPosition.x - 42f,
                                coinPosition.y - 25f
                            )
                        )

                        sparklePositions.forEach { sparkle ->

                            drawCircle(
                                color = Color.White.copy(
                                    alpha = glitterAlpha
                                ),
                                radius = 4f * glitterScale,
                                center = sparkle
                            )

                            drawLine(
                                color = Color.White.copy(
                                    alpha = glitterAlpha * 0.8f
                                ),
                                start = Offset(
                                    sparkle.x - 9f,
                                    sparkle.y
                                ),
                                end = Offset(
                                    sparkle.x + 9f,
                                    sparkle.y
                                ),
                                strokeWidth = 2f
                            )

                            drawLine(
                                color = Color.White.copy(
                                    alpha = glitterAlpha * 0.8f
                                ),
                                start = Offset(
                                    sparkle.x,
                                    sparkle.y - 9f
                                ),
                                end = Offset(
                                    sparkle.x,
                                    sparkle.y + 9f
                                ),
                                strokeWidth = 2f
                            )
                        }
                    }


                    // ANIMATION
                    withTransform({

                        scale(
                            scale = coinScale,
                            pivot = coinPosition
                        )

                    }) {

                        drawImage(
                            image = coinImage,

                            dstOffset = IntOffset(
                                x = (
                                        coinPosition.x -
                                                coinSize / 2f
                                        ).toInt(),

                                y = (
                                        coinPosition.y -
                                                coinSize / 2f
                                        ).toInt()
                            ),

                            dstSize = IntSize(
                                width = coinSize.toInt(),
                                height = coinSize.toInt()
                            )
                        )
                    }
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
    heightDp = 800
)
@Composable
fun GameScreenPreview() {
    GameScreen(
        onBackClick = {},
        startLevel = 8
    )
}

