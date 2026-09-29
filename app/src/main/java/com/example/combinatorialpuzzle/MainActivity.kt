package com.example.combinatorialpuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.combinatorialpuzzle.ui.theme.CombinatorialPuzzleTheme
import com.example.combinatorialpuzzle.ui1.GameScreen
import com.example.combinatorialpuzzle.ui1.SettingsScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            CombinatorialPuzzleTheme {

                Greeting(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


@Composable
fun Greeting(
    modifier: Modifier = Modifier
) {

    var currentScreen by remember {
        mutableStateOf("home")
    }


    when (currentScreen) {

        // =================================================
        // HOME SCREEN
        // =================================================

        "home" -> {

            Box(
                modifier = modifier.fillMaxSize()
            ) {

                // =================================================
                // BACKGROUND IMAGE
                // =================================================

                Image(
                    painter = painterResource(
                        R.drawable.game_background1
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )


                // =================================================
                // HOME CONTENT
                // =================================================

                Column(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center

                ) {

                    Text(
                        text = "🧩",
                        fontSize = 100.sp
                    )


                    Text(
                        text = "Welcome to",

                        style =
                            MaterialTheme.typography.headlineMedium,

                        color = Color.White
                    )


                    Text(
                        text = "Combinatorial Puzzle Game",

                        style =
                            MaterialTheme.typography.headlineMedium,

                        fontWeight =
                            FontWeight.Bold,

                        color = Color(0xFFFF9800),

                        textAlign = TextAlign.Center
                    )


                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )


                    Text(
                        text =
                            "Challenge your mind with exciting\ncombinatorial puzzles!",

                        style =
                            MaterialTheme.typography.bodyLarge,

                        color = Color.White,

                        textAlign = TextAlign.Center
                    )


                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )


                    // =================================================
                    // PLAY BUTTON
                    // =================================================

                    Button(
                        onClick = {
                            currentScreen = "game"
                        }
                    ) {

                        Text(
                            text = "PLAY"
                        )
                    }


                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )


                    // =================================================
                    // SETTINGS BUTTON
                    // =================================================

                    Button(
                        onClick = {
                            currentScreen = "settings"
                        }
                    ) {

                        Text(
                            text = "SETTINGS"
                        )
                    }
                }
            }
        }


        // =================================================
        // GAME SCREEN
        // =================================================

        "game" -> {

            GameScreen(
                onBackClick = {
                    currentScreen = "home"
                }
            )
        }


        // =================================================
        // SETTINGS SCREEN
        // =================================================

        "settings" -> {

            SettingsScreen(
                onBackClick = {
                    currentScreen = "home"
                }
            )
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
fun HomeScreenPreview() {

    CombinatorialPuzzleTheme {

        Greeting(
            modifier = Modifier.fillMaxSize()
        )
    }
}