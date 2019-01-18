package ru.vladimirov.rockettest.rockettest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

class AlgView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private var arrayWidth:Int? = null
    private var arrayHeight:Int? = null
    private var array: Array<IntArray> ? = null
    private var speed:Int = 3

    var alg:Int = 1

    private var verticalMode:Boolean = true
    private var primaryColor:Int = 0
    private var primaryDarkColor:Int = 0
    private var accentColor:Int = 0
    private var whiteColor:Int = 0

    init {
        initialize(40, 10, true, 10, 1)

        primaryColor = resources.getColor(R.color.colorPrimary)
        primaryDarkColor = resources.getColor(R.color.colorPrimaryDark)
        accentColor = resources.getColor(R.color.colorAccent)
        whiteColor = resources.getColor(R.color.colorWhite)
    }

    fun initialize(width:Int, height:Int, verticalMode:Boolean, speed:Int, alg:Int) {
        this.verticalMode = verticalMode
        this.arrayWidth = width
        this.arrayHeight = height
        this.speed = speed
        this.alg = alg

        array = Array(arrayWidth!!) { IntArray(arrayHeight!!) }

        for (i in array!!.indices) {
            for (j in array!![i].indices) {
                array!![i][j] = (0..1).random()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth: Int
        val desiredHeight: Int

        if (verticalMode) {
            desiredWidth = maxWidth
            desiredHeight = maxWidth * arrayHeight!! / arrayWidth!!

        } else {
            desiredWidth = maxHeight * arrayWidth!! / arrayHeight!!
            desiredHeight = maxHeight
        }

        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    private val paint = Paint()

    private fun getCellSize() : Float {
        return width.toFloat() / arrayWidth!!
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val cellSize = getCellSize()

        for (i in array!!.indices) {
            for (j in array!![i].indices) {
                if (array!![i][j] == 0) {
                    paint.color = whiteColor
                } else {
                    paint.color = primaryColor
                }

                canvas.drawRect(i * cellSize, j * cellSize, i * cellSize + cellSize, j * cellSize + cellSize, paint)
            }
        }

        if (targetX != -1 && targetY != -1) {

            paint.color = accentColor

            canvas.drawRect(targetX * cellSize, targetY * cellSize, targetX * cellSize + cellSize, targetY * cellSize + cellSize, paint)
        }

    }

    private var currentAlg:BaseAlg? = null

    private var targetX = -1
    private var targetY = -1

    private var startX = -1
    private var startY = -1

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val cellSize = getCellSize()

        targetX = (event.x.toInt() / cellSize).toInt()
        targetY = (event.y.toInt() / cellSize).toInt()

        when (event.action) {

            MotionEvent.ACTION_UP -> {
                println("ACTION_UP")

                startX = targetX
                startY = targetY

                targetX = -1
                targetY = -1

                currentAlg = arrayOf(Alg1(), Alg2(), Alg3())[alg]
                currentAlg!!.start()
            }

            MotionEvent.ACTION_DOWN -> {
                println("ACTION_UP")

//                currentAlg?.stop()
            }

            MotionEvent.ACTION_MOVE -> {
                println("ACTION_MOVE")

            }

        }

        invalidate()

        return true
    }

    abstract inner class BaseAlg : Runnable {

        private var thread:Thread? = null

        @Volatile
        protected var started:Boolean = false

        fun start() {
            if (started) return

            step = 0

            started = true

            thread = Thread(this)
            thread!!.start()
        }

        fun stop() {
            started = false
            thread?.interrupt()
        }

        private var step:Int = 0

        override fun run() {
            println("Alg started")

            while(started) {

                println("Step $step")

                val finished = doAlgStep(step++)

                handler?.post {
                    invalidate()
                }

                if (finished) break

                try {
                    Thread.sleep(1000L / speed)
                } catch (e:InterruptedException) {
                }
            }

            started = false

            println("Alg finished")
        }

        abstract fun doAlgStep(step:Int) : Boolean
    }

    abstract inner class BaseRecursiveAlg: BaseAlg() {

        override fun run() {
            println("Recursive alg started")

            doRecursiveAlgStep()

            started = false

            println("Recursive alg finished")

        }

        open fun doRecursiveAlgStep() {
            if (!started) return

            handler?.post {
                invalidate()
            }

            try {
                Thread.sleep(1000L / speed)
            } catch (e:InterruptedException) {
            }
        }

        override fun doAlgStep(step: Int) : Boolean {
            return false
        }

    }

    inner class Alg1 : BaseRecursiveAlg() {
        override fun doRecursiveAlgStep() {

            println("new step $startX, $startY")

            // Проверка выхода за допустимый диапазон

            if (!checkMargins(startX, startY)) return

            // Если цвет элемента не заменяемый цвет, возврат

            if (array!![startX][startY] == 1) return

            // Установить цвет элемента в цвет заливки.

            array!![startX][startY] = 1


            super.doRecursiveAlgStep()

            // Подготовка направлений для следующих шагов

            val westX = startX - 1
            val westY = startY

            val northX = startX
            val northY = startY - 1

            val eastX = startX + 1
            val eastY = startY

            val southX = startX
            val southY = startY + 1

            // Шаг на запад от элемента

            startX = westX
            startY = westY

            doRecursiveAlgStep()

            // Шаг на север от элемента

            startX = northX
            startY = northY

            doRecursiveAlgStep()

            // Шаг на восток от элемента

            startX = eastX
            startY = eastY

            doRecursiveAlgStep()

            // Шаг на юг от элемента

            startX = southX
            startY = southY

            doRecursiveAlgStep()

        }

    }

    private fun checkMargins(x:Int, y:Int) : Boolean {
        return x >= 0 && x < array!!.size && y >= 0 && y < array!![x].size
    }

    inner class Alg2 : BaseAlg() {

        inner class Pair(val x:Int, val y:Int)

        private val queue = LinkedList<Pair>()

        override fun doAlgStep(step: Int): Boolean {

            // Инициализируем очередь

            if (step == 0) {
                queue.addFirst(Pair(startX, startY))
            }

            // Если очередь пуста, закончить
            if (queue.isEmpty()) return true

            // Берем первый элемент из очереди
            val last = queue.removeFirst()

            // Если оне не выходит за гарницы массива и его цвет заменяемый
            if (checkMargins(last.x, last.y) && array!![last.x][last.y] == 0) {

                // Поменять цвет на заменяемый
                array!![last.x][last.y] = 1

                // Добавить всех соседей в очередь
                queue.add(Pair(last.x - 1, last.y))
                queue.add(Pair(last.x + 1, last.y))
                queue.add(Pair(last.x, last.y - 1))
                queue.add(Pair(last.x, last.y + 1))
            }

            return false
        }

    }

    inner class Alg3 : BaseAlg() {

        inner class Pair(val x:Int, val y:Int)

        private val queue = LinkedList<Pair>()

        override fun doAlgStep(step: Int): Boolean {

            if (step == 0) {
                // Если начальный элемент желаемого цвета, завершить
                if (array!![startX][startY] == 1) return true

                // Инициализируем очередь
                queue.add(Pair(startX, startY))
            }

            // Если очередь пуста, закончить
            if (queue.isEmpty()) return true

            // Берем первый элемент из очереди
            val last = queue.removeFirst()

            var xl = last.x
            var xr = last.x

            // Смещаемся влево до тех пор, пока цвет не станет отличаться от заменяемого цвета
            while (xl - 1 >= 0 && array!![xl - 1][last.y] == 0) xl--

            // Смещаемся вправо до тех пор, пока цвет не станет отличаться от заменяемого цвета
            while (xr + 1 < arrayWidth!! && array!![xr + 1][last.y] == 0) xr++

            for (x in (xl..xr)) {
                // Всем элементам в найденом диапазоне присваиваем заменяемый цвет
                array!![x][last.y] = 1

                // Все элементы над найденым диапазоном добавляем в очередь, если цвет отличается от заменяемого
                val y1 = last.y - 1
                if (y1 >= 0 && array!![x][y1] == 0) queue.addLast(Pair(x, y1))

                // Все элементы под найденым диапазоном добавляем в очередь, если цвет отличается от заменяемого
                val y2 = last.y + 1
                if (y2 < arrayHeight!! && array!![x][y2] == 0) queue.addLast(Pair(x, y2))
            }

            return false
        }

    }

}
