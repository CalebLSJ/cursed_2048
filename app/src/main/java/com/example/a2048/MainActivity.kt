package com.example.a2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2048.ui.theme._2048Theme
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val gameModel = GameModel(this)
        setContent {
            _2048Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        gameModel = gameModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GameScreen(gameModel: GameModel, modifier: Modifier = Modifier) {
    var gameState by remember { mutableStateOf(gameModel.getGameState()) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Collapsible Menu
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuExpanded = !menuExpanded }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Game Settings", fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = if (menuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Menu"
                    )
                }
                AnimatedVisibility(
                    visible = menuExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hypermerge Mode")
                        Switch(
                            checked = gameState.hypermergeEnabled,
                            onCheckedChange = {
                                gameModel.toggleHypermerge(it)
                                gameState = gameModel.getGameState()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "2048", fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Text(text = "Score: ${gameState.score}", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp))
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if (abs(x) > abs(y)) {
                                if (x > 10) {
                                    if (gameModel.move(Direction.RIGHT)) {
                                        gameState = gameModel.getGameState()
                                    }
                                } else if (x < -10) {
                                    if (gameModel.move(Direction.LEFT)) {
                                        gameState = gameModel.getGameState()
                                    }
                                }
                            } else {
                                if (y > 10) {
                                    if (gameModel.move(Direction.DOWN)) {
                                        gameState = gameModel.getGameState()
                                    }
                                } else if (y < -10) {
                                    if (gameModel.move(Direction.UP)) {
                                        gameState = gameModel.getGameState()
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in gameState.grid) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (cell in row) {
                            Tile(cell, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (gameState.isGameOver) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Game Over!", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Button(onClick = {
                gameModel.reset()
                gameState = gameModel.getGameState()
            }) {
                Text("Restart")
            }
        }
    }
}

@Composable
fun Tile(value: Int, modifier: Modifier = Modifier) {
    val backgroundColor = remember(value) {
        when (value) {
            0 -> Color(0xFFCDC1B4)
            2 -> Color(0xFFEEE4DA)
            4 -> Color(0xFFEDE0C8)
            8 -> Color(0xFFF2B179)
            16 -> Color(0xFFF59563)
            32 -> Color(0xFFF67C5F)
            64 -> Color(0xFFF65E3B)
            128 -> Color(0xFFEDCF72)
            256 -> Color(0xFFEDCC61)
            512 -> Color(0xFFEDC850)
            1024 -> Color(0xFFEDC53F)
            2048 -> Color(0xFFEDC22E)
            else -> {
                // Generative colors for the "cursed" values
                val extraColors = listOf(
                    Color(0xFF7CB342), // Green
                    Color(0xFF00ACC1), // Cyan
                    Color(0xFF1E88E5), // Blue
                    Color(0xFF5E35B1), // Deep Purple
                    Color(0xFFD81B60), // Pink
                    Color(0xFFF4511E), // Deep Orange
                    Color(0xFF00897B), // Teal
                    Color(0xFF43A047), // Dark Green
                    Color(0xFFFB8C00), // Orange
                    Color(0xFF3949AB)  // Indigo
                )
                val index = (value.toString().hashCode() % extraColors.size).let { if (it < 0) it + extraColors.size else it }
                extraColors[index]
            }
        }
    }

    val textColor = if (value == 0) Color.Transparent else if (backgroundColor.luminance() > 0.5f) Color(0xFF776E65) else Color.White

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                fontSize = if (value < 100) 24.sp else if (value < 1000) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
