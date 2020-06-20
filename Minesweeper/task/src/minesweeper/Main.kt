package minesweeper
import java.util.Scanner
import kotlin.random.Random

abstract class Cell(var open: Boolean = false, var marked: Boolean = false) {
    abstract fun toOpenChar():Char
    fun toChar() = if (open) toOpenChar() else if (marked) '*' else '.'
}

class EmptyCell(var numberOfMines: Int = 0) : Cell() {

    override fun toOpenChar() = when (numberOfMines){
            0 -> '/'
            else -> numberOfMines.toString()[0]
    }
}

class MineCell : Cell() {
    override fun toOpenChar() = 'X'
}

class MoveResult(val message:String? = null, val endGame:Boolean = false, val boardChanged:Boolean = false)

class Field(var fieldArray: Array<Array<Cell>>){
    private var gameStarted = false
    fun show(){
        println(" │123456789│")
        println("—│—————————│")
        for (y in fieldArray.indices) {
            print(y+1)
            print('|')
            for (x in fieldArray[y]){
                print(x.toChar())
            }
            println('|')
        }
        println("—│—————————│")
    }
    fun add(x:Int, y:Int): Boolean{
        if (!canAdd(x,y)) return false
        fieldArray[y][x] = MineCell()
        return true
    }
    private fun canAdd(x:Int, y:Int): Boolean = when(fieldArray[y][x]){
        is MineCell -> false
        is EmptyCell -> true
        else -> throw Exception()
    }

    fun startGame(numberOfMines: Int, startX:Int, startY:Int){
        repeat(numberOfMines) {
            while (true) {
                val y = Random.nextInt(9)
                val x = Random.nextInt(9)
                if (!(startX == x && startY == y) && add(x,y)){
                    break
                }
            }
        }
        gameStarted = true
        val movex = intArrayOf(1,0,-1,-1,-1,0,1,1)
        val movey = intArrayOf(1,1,1,0,-1,-1,-1,0)
        for (i in fieldArray.indices){
            for (j in fieldArray[i].indices){
                if (fieldArray[i][j] is MineCell) {
                    for (moveIdx in movex.indices) {
                        val x = i + movex[moveIdx]
                        val y = j + movey[moveIdx]
                        if (x >= 0 && x < fieldArray.size
                                && y >= 0 && y < fieldArray[x].size
                                && fieldArray[x][y] is EmptyCell) {
                            (fieldArray[x][y] as EmptyCell).numberOfMines++
                        }
                    }
                }
            }
        }
    }

    private fun checkWinMarks() : Boolean {
        for (row in fieldArray) {
            for (cell in row) {
                if (cell is MineCell && !cell.marked || cell is EmptyCell && cell.marked) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkWinFree() : Boolean {
        for (row in fieldArray) {
            for (cell in row) {
                if (cell is EmptyCell && !cell.open) return false
            }
        }
        return true
    }

    fun markCells(x: Int, y: Int): MoveResult {
        val cell = fieldArray[y][x]
        cell.marked = !cell.marked
        return when {
            checkWinMarks() -> MoveResult("Congratulations! You found all the mines!", true)
            
            cell is EmptyCell || cell is MineCell -> MoveResult(boardChanged = true)
            else -> throw NotImplementedError()
        }
    }

    private fun waveFree(x:Int, y:Int){
        when (val cell = fieldArray[y][x]) {
            is MineCell -> return
            is EmptyCell -> {
                cell.open = true

                val movex = intArrayOf(1, 0, -1, 0)
                val movey = intArrayOf(0, 1, 0, -1)
                for (i in movex.indices) {
                    val nx = x + movex[i]
                    val ny = y + movey[i]
                    if (nx >= 0 && nx < fieldArray.size
                            && ny >= 0 && ny < fieldArray[nx].size
                            && !fieldArray[ny][nx].open
                            && fieldArray[ny][nx] is EmptyCell) {
                        waveFree(nx, ny)
                    }
                }

            }
        }
    }

    fun freeCells(x:Int, y:Int): MoveResult{
        val cell = fieldArray[y][x]
        cell.open = true
        return when (cell) {
            is MineCell -> MoveResult("You stepped on a mine and failed!", true)
            is EmptyCell -> {
                if (cell.numberOfMines == 0)
                    waveFree(x, y)
                if (checkWinFree()) MoveResult("Congratulations! You found all the mines!", true)
                else MoveResult(boardChanged = true)
            }
            else -> throw Exception()
        }
    }
}

fun main() {
    val scanner = Scanner(System.`in`)

    val arr = Array<Array<Cell>>(9) { Array<Cell>(9) { _ -> EmptyCell() }}
    val field = Field(arr)
    print("How many mines do you want on the field? > ")
    val numberOfMines = scanner.nextInt()



    var prevMove: MoveResult? = null
    while(prevMove == null || !prevMove.endGame) {
        when {
            prevMove == null || prevMove.boardChanged -> field.show()
            !prevMove.boardChanged -> println(prevMove.message)
        }
        print("Set/unset mine marks or claim a cell as free: > ")
        val x = scanner.nextInt()
        val y = scanner.nextInt()
        val query = scanner.next()
        prevMove = when (query) {
            "mine" -> field.markCells(x - 1, y - 1)
            "free" -> {
                if (prevMove == null) field.startGame(numberOfMines, x - 1, y - 1)
                field.freeCells(x - 1, y - 1)
            }
            else -> throw Exception(query)
        }
    }

    field.show()
    println("Congratulations! You found all the mines!")
}
