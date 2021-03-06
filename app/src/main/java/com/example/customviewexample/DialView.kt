package com.example.customviewexample

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when(this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35


class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr : Int = 0
) : View(context, attrs, defStyleAttr) {

    // フラグ定数 これを指定するとアンチエイリアス機能付きで、図形が描画されます
    // Paintクラスを使用することで、形を描くことができる
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var radius = 0.0f
    private var fanSpeed = FanSpeed.OFF
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedMaxColor = 0

    init {
        isClickable = true

        // initで以下の処理が漏れていた
        val typedArray = context.obtainStyledAttributes(attrs,R.styleable.DialView)
        fanSpeedLowColor=typedArray.getColor(R.styleable.DialView_fanColor1,0)
        fanSpeedMediumColor = typedArray.getColor(R.styleable.DialView_fanColor2,0)
        fanSpeedMaxColor = typedArray.getColor(R.styleable.DialView_fanColor3,0)
        typedArray.recycle()
    }

    // クリックイベントをトリガーに処理を実行する
    override fun performClick(): Boolean {
        if (super.performClick()) return true
        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        // invalidate()を記述しないと実行されない
        invalidate()
        return true
    }

    // customViewのサイズが変わる
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2.0 * 0.6).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = when (fanSpeed)  {
           FanSpeed.OFF -> Color.GRAY
           FanSpeed.LOW -> fanSpeedLowColor
           FanSpeed.MEDIUM -> fanSpeedMediumColor
           FanSpeed.HIGH -> fanSpeedMaxColor
        }

        canvas.drawCircle((width/2).toFloat(), (height / 2).toFloat(), radius, paint)

        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }

    // PointFの型に対してメソッドを定義している
    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        val startAngle = Math.PI * (9 / 8.0)
        // startAngle + pos.ordinalがstartAngle * pos.ordinalになっていた
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2

        // 以下がwidthになっていた
        y = (radius * sin(angle)).toFloat() + height / 2
    }

}