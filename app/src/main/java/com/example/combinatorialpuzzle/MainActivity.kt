package com.example.combinatorialpuzzle

import android.os.Bundle
import com.example.combinatorialpuzzle.ui1.GameScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.combinatorialpuzzle.ui.theme.CombinatorialPuzzleTheme
import com.example.combinatorialpuzzle.ui1.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CombinatorialPuzzleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Greeting(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier) {
    var currentScreen by remember {
        mutableStateOf("home")
    }

    when (currentScreen) {

        "home" -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text ="🧩",
                    fontSize = 120.sp,

                )

                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Red
                )

                Text(
                    text = "Combinatorial Puzzle Game",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Challenge your mind with exciting \n combinatorial puzzles!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        currentScreen = "game"
                    }
                ) {
                    Text("PLAY")
                }

                Button(
                    onClick = {
                        currentScreen = "settings"
                    }
                ) {
                    Text("SETTINGS")
                }

            }
        }
        "game" -> GameScreen(
            onBackClick ={
                currentScreen = "home"
            }
        )
        "settings" -> SettingsScreen(
            onBackClick = {
                currentScreen = "home"
            }
        )
    }
}
