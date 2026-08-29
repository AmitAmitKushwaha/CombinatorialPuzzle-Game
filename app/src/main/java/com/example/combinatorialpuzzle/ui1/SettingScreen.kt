package com.example.combinatorialpuzzle.ui1

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    Column( modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ){
        Text(
            text = "⚙ Setting",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                onBackClick()
            },
                    modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }

























    }

}