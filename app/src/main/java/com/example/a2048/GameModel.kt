package com.example.a2048

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class GameState(
    val grid: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val hypermergeEnabled: Boolean = false
)

class GameModel(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("2048_prefs", Context.MODE_PRIVATE)
    private var grid = MutableList(4) { MutableList(4) { 0 } }
    private var score = 0
    var hypermergeEnabled = false

    init {
        loadGame()
    }

    private fun saveGame() {
        val gridString = grid.flatten().joinToString(",")
        prefs.edit().apply {
            putString("grid", gridString)
            putInt("score", score)
            putBoolean("hypermerge", hypermergeEnabled)
            apply()
        }
    }

    private fun loadGame() {
        val gridString = prefs.getString("grid", null)
        if (gridString != null) {
            val values = gridString.split(",").map { it.toInt() }
            for (i in 0 until 4) {
                for (j in 0 until 4) {
                    grid[i][j] = values[i * 4 + j]
                }
            }
            score = prefs.getInt("score", 0)
            hypermergeEnabled = prefs.getBoolean("hypermerge", false)
        } else {
            reset()
        }
    }

    fun getGameState() = GameState(
        grid = grid.map { it.toList() },
        score = score,
        isGameOver = isGameOver(),
        hypermergeEnabled = hypermergeEnabled
    )

    fun move(direction: Direction): Boolean {
        var moved = false
        val newGrid = MutableList(4) { MutableList(4) { 0 } }

        when (direction) {
            Direction.LEFT -> {
                for (i in 0 until 4) {
                    val row = grid[i].filter { it != 0 }.toMutableList()
                    val mergedRow = merge(row)
                    for (j in 0 until 4) {
                        newGrid[i][j] = if (j < mergedRow.size) mergedRow[j] else 0
                    }
                }
            }
            Direction.RIGHT -> {
                for (i in 0 until 4) {
                    val row = grid[i].filter { it != 0 }.reversed().toMutableList()
                    val mergedRow = merge(row)
                    for (j in 0 until 4) {
                        newGrid[i][3 - j] = if (j < mergedRow.size) mergedRow[j] else 0
                    }
                }
            }
            Direction.UP -> {
                for (j in 0 until 4) {
                    val col = (0 until 4).map { grid[it][j] }.filter { it != 0 }.toMutableList()
                    val mergedCol = merge(col)
                    for (i in 0 until 4) {
                        newGrid[i][j] = if (i < mergedCol.size) mergedCol[i] else 0
                    }
                }
            }
            Direction.DOWN -> {
                for (j in 0 until 4) {
                    val col = (0 until 4).map { grid[it][j] }.filter { it != 0 }.reversed().toMutableList()
                    val mergedCol = merge(col)
                    for (i in 0 until 4) {
                        newGrid[3 - i][j] = if (i < mergedCol.size) mergedCol[i] else 0
                    }
                }
            }
        }

        if (newGrid != grid) {
            grid = newGrid
            moved = true
            spawnTile()
            saveGame()
        }

        return moved
    }

    private fun merge(line: MutableList<Int>): List<Int> {
        val result = mutableListOf<Int>()
        var i = 0
        while (i < line.size) {
            if (i + 1 < line.size) {
                val val1 = line[i]
                val val2 = line[i + 1]
                val shouldMerge = if (hypermergeEnabled) {
                    val ratio = val1.toDouble() / val2.toDouble()
                    ratio in 0.5..2.0
                } else {
                    val1 == val2
                }

                if (shouldMerge) {
                    val newValue = val1 + val2
                    result.add(newValue)
                    score += newValue
                    i += 2
                } else {
                    result.add(val1)
                    i++
                }
            } else {
                result.add(line[i])
                i++
            }
        }
        return result
    }

    private fun spawnTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                if (grid[i][j] == 0) emptyCells.add(i to j)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
            grid[r][c] = if (Random.nextDouble() < 0.9) 2 else 4
        }
    }

    private fun isGameOver(): Boolean {
        if (grid.any { row -> row.any { it == 0 } }) return false
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                val val1 = grid[i][j]
                if (j < 3) {
                    val val2 = grid[i][j+1]
                    if (hypermergeEnabled) {
                        if (val1.toDouble() / val2.toDouble() in 0.5..2.0) return false
                    } else if (val1 == val2) return false
                }
                if (i < 3) {
                    val val2 = grid[i+1][j]
                    if (hypermergeEnabled) {
                        if (val1.toDouble() / val2.toDouble() in 0.5..2.0) return false
                    } else if (val1 == val2) return false
                }
            }
        }
        return true
    }

    fun reset() {
        grid = MutableList(4) { MutableList(4) { 0 } }
        score = 0
        spawnTile()
        spawnTile()
        saveGame()
    }

    fun toggleHypermerge(enabled: Boolean) {
        hypermergeEnabled = enabled
        saveGame()
    }
}
