package com.example.combinatorialpuzzle.ui1

import androidx.compose.ui.graphics.drawscope.rotate
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
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.FilterQuality


// AUTOMATIC LEVEL DETECTION

fun getTotalLevels(context: Context): Int {
    return context.assets.list("")?.count { fileName ->
        fileName.matches(Regex("PuzzleGraph\\d+\\.json"))
    } ?: 0
}

// =====================================================
// GAME SCREEN
// =====================================================

@Composable
fun GameScreen(
    onBackClick: () -> Unit,
    startLevel: Int = 1
) {


    val context = LocalContext.current

    val totalLevels = remember {
        getTotalLevels(context)
    }
    // =================================================
    // LEVEL
    // =================================================

    var level by remember {
        mutableIntStateOf(startLevel)
    }

    //RedoRequest

    var redoRequest by remember {
        mutableIntStateOf(0)
    }

    //UndoRequest

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
        "👆 Drag the fox/bear to a connected adjacent node.\n" +
                "🚫 A move is blocked if another fox/bear is next to the destination node.\n" +
                "🎯 Reach the cave to complete the level!"


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
        delay(2500.milliseconds)


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
                R.drawable.screen_background
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
            redoRequest = redoRequest,
            onWinStateChange = { won = it },
            gameWon = won,
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
                        end = 12.dp,
                        bottom = 80.dp
                    )
                    .size(
                        width = 180.dp,
                        height = 80.dp
                    )
                    .clickable {


                        if (level < totalLevels) {
                            level++

                            // Reset win state
                            won = false
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


                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧩",
                        color = Color(0xFFDAA520),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Level text
                    Text(
                        text = " Level $level",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            // =================================================
            // WIN MESSAGE
            // =================================================

            if (won) {

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = 100.dp
                        )
                        .size(
                            width = 280.dp,
                            height = 125.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        painter = painterResource(R.drawable.youwin),
                        contentDescription = "Undo button",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "YOU WIN",
                        color = Color(0xFFFFD700),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center

                    )

                }

            }


            // =================================================
            // BACK BUTTON
            // =================================================

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 12.dp,
                        bottom = 80.dp
                    )
                    .size(
                        width = 180.dp,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⬅",
                        color = Color(0xFFDAA520),
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-9).dp)
                    )

                    Text(
                        text = " Back",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }


            // =================================================
            // UNDO BUTTON
            // =================================================

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 12.dp,
                        bottom = 160.dp
                    )
                    .size(
                        width = 180.dp,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↩",
                        color = Color(0xFFDAA520),
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-9).dp)
                    )

                    Text(
                        text = " Undo",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }



        // =================================================
        // REDO BUTTON
        // =================================================

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 12.dp,
                    bottom = 160.dp
                )
                .size(
                    width = 180.dp,
                    height = 80.dp
                )
                .clickable {
                    redoRequest++
                },
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(R.drawable.level),
                contentDescription = "Redo button",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "↪",
                    color = Color(0xFFDAA520),
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-9).dp)
                )

                Text(
                    text = " Redo",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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
    gameWon: Boolean,
    undoRequest: Int,
    redoRequest: Int,
    onWinStateChange: (Boolean) -> Unit,
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

    // =================================================
    // HIGHLIGHTED EDGE
    // =================================================

    var highlightedEdges by remember(jsonFileName) {
        mutableStateOf<Set<Pair<Int, Int>>>(emptySet())
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


    val redoHistory = remember(jsonFileName) {
        mutableStateListOf<List<Int>>()
    }


    val moveHistory = remember(jsonFileName) {
        mutableStateListOf<List<Int>>()
    }


    LaunchedEffect(undoRequest) {

        if (
            undoRequest > 0 &&
            moveHistory.isNotEmpty() &&
            !isMoving
        ) {

            redoHistory.add(coinNodes.toList())

            // Take the most recent state
            coinNodes = moveHistory.removeAt(
                moveHistory.lastIndex
            )

            selectedCoinIndex = -1
            highlightedNodes = emptySet()
            highlightedEdges = emptySet()
            blockedCoinIndices = emptySet()

            winTriggered = false
            isMoving = false

            // If the win state was undone,
            // unlock the game

            onUndo()
        }
    }

    LaunchedEffect(redoRequest) {
        if (
            redoRequest > 0 &&
            redoHistory.isNotEmpty() &&
            !isMoving
        ) {
            moveHistory.add(coinNodes.toList())

            coinNodes = redoHistory.removeAt(
                redoHistory.lastIndex
            )

            onWinStateChange(
                targetCoinIndex != -1 &&
                        coinNodes[targetCoinIndex] == graph.winNode
            )

            selectedCoinIndex = -1
            highlightedNodes = emptySet()
            blockedCoinIndices = emptySet()
        }
    }



    val coroutineScope = rememberCoroutineScope()

    // =================================================
    // APPLE IMAGE + ANIMATION
    // =================================================

    val appleImage = ImageBitmap.imageResource(R.drawable.fox2)

    val logImage = ImageBitmap.imageResource(R.drawable.bear)

    val basketImage = ImageBitmap.imageResource(R.drawable.foxden)

    val greenNodeImage = ImageBitmap.imageResource(R.drawable.green_node)

    val blackNodeImage = ImageBitmap.imageResource(R.drawable.blacknode)

    val jungleEdgeImage = ImageBitmap.imageResource(R.drawable.jungle_edge3)



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

        val reachableEdges = mutableSetOf<Pair<Int, Int>>()

        while (queue.isNotEmpty()) {

            val currentNode = queue.removeFirst()

            val neighbors = getAdjacentNodes(currentNode)

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

                // Mark this node as reachable

                visited.add(nextNode)
                queue.add(nextNode)

                // Highlight only the edge used to discover this node
                reachableEdges.add(
                    Pair(
                        minOf(currentNode, nextNode),
                        maxOf(currentNode, nextNode)
                    )
                )
            }
        }

        // =================================================
        // DESTINATION NODES
        // =================================================

        val reachableNodes = visited
            .filter { it != startNode }
            .toSet()

        // =================================================
        // RETURN ONLY NECESSARY BFS EDGES
        // =================================================

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

        val coinIndex = selectedCoinIndex


        if (coinIndex == -1) { return }


        val finalDestination = destinationNode

        // =================================================
        // DESTINATION MUST BE GREEN
        // =================================================

        if (destinationNode !in highlightedNodes) { return }


        // =================================================
        // FIND PATH
        // =================================================

        val startNode = coinNodes[coinIndex]


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

       // Save current position before making the move

        moveHistory.add(coinNodes.toList())
        redoHistory.clear()

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

        highlightedEdges = emptySet()

        blockedCoinIndices = emptySet()

        isMoving = false



        // WIN ONLY IF THE SELECTED FINAL DESTINATION IS winNode

        if (
            coinIndex == targetCoinIndex &&
            finalDestination == graph.winNode
        ) {
            winTriggered = true
            onWin()
        }

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

                highlightedEdges = result.second
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



    // =================================================
    // GRAPH BOX
    // =================================================

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(
                start = 25.dp,
                end = 25.dp,
                bottom = 30.dp
            ),
        contentAlignment = Alignment.Center
    ) {

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

                        // Do not allow any game action after winning
                        if (gameWon) {
                            return@awaitEachGesture
                        }


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
            // DRAW EDGES
            // ==========================================
            graph.edges.forEach { edge ->

                val node1 = nodeMap[edge.first]
                val node2 = nodeMap[edge.second]

                if (node1 != null && node2 != null) {

                    val start = position(node1)
                    val end = position(node2)

                    val dx = end.x - start.x
                    val dy = end.y - start.y

                    val length = kotlin.math.sqrt(dx * dx + dy * dy)
                    val angle = Math.toDegrees(
                        kotlin.math.atan2(dy, dx).toDouble()
                    ).toFloat()

                    val edgeKey = Pair(
                        minOf(edge.first, edge.second),
                        maxOf(edge.first, edge.second)
                    )

                    val isGreenEdge = edgeKey in highlightedEdges


                    // Always draw the jungle texture first

                    val edgeThickness = 99f

                    val center = Offset(
                        x = (start.x + end.x) / 2f,
                        y = (start.y + end.y) / 2f
                    )

                    rotate(
                        degrees = angle,
                        pivot = center
                    ) {
                        drawImage(
                            image = jungleEdgeImage,
                            dstOffset = IntOffset(
                                x = (center.x - length / 2f).toInt(),
                                y = (center.y - edgeThickness / 2f).toInt()
                            ),
                            dstSize = IntSize(
                                width = length.toInt(),
                                height = edgeThickness.toInt()
                            ),
                            filterQuality = FilterQuality.High
                        )
                    }

                   // Draw green highlight over the jungle path

                    if (isGreenEdge) {
                        drawLine(
                            color = Color.Green.copy(alpha = greenAlpha * 0.45f),
                            start = start,
                            end = end,
                            strokeWidth = 22f
                        )

                        drawLine(
                            color = Color(0xFF76FF03).copy(alpha = greenAlpha),
                            start = start,
                            end = end,
                            strokeWidth = 5f
                        )
                    }
                }
            }




            // ==========================================
            // DRAW NODES
            // ==========================================

            graph.nodes.forEach { node ->

                val nodePosition = position(node)

                val isWinNode =
                    node.id == graph.winNode

                if (isWinNode) {

                    // Is the basket currently reachable?

                    val isBasketReachable =
                        node.id in highlightedNodes




                    val basketSize = 70f * basketScale
                    val basketY = nodePosition.y + basketFloat


                  // Golden glow always fluctuates
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(
                            alpha = glitterAlpha * 0.45f
                        ),
                        radius = basketSize / 2f + 12f * glitterScale,
                        center = Offset(nodePosition.x, basketY)
                    )


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

                    val baseNodeSize = 45f

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

                val currentNodeId = coinNodes[coinIndex]

                val node = nodeMap[currentNodeId]

                if (node != null) {

                    val normalPosition = position(node)

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

                    val coinSize = when {
                        isSelected -> 85f
                        isTargetCoin -> 75f * basketScale
                        else -> 75f
                    }

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

                        // Appearing red outer glow

                        drawCircle(
                            color = Color.Red.copy(
                                alpha = redAlpha * 0.25f
                            ),
                            radius = coinSize / 2f + 12f,
                            center = coinPosition
                        )

                        // Appearing red border

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

                    // Golden glow around Apple

                    if (isTargetCoin) {
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(
                                alpha = glitterAlpha * 0.45f
                            ),
                            radius = coinSize / 2f + 12f * glitterScale,
                            center = coinPosition
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
                            radius = 50f * glitterScale,
                            center = coinPosition
                        )

                        drawCircle(
                            color = Color(0xFFFFC107)
                                .copy(alpha = glitterAlpha),
                            radius = 45f,
                            center = coinPosition,
                            style = Stroke(
                                width = 5f
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