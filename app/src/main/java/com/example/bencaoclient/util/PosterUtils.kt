package com.example.bencaoclient.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

/** 分享海报 slogan 备选，每次随机一条。 */
val BencaoSharePosterSlogans = listOf(
    "一花一世界，一叶一温柔",
    "人间草木，皆有温柔诗意",
    "草木有本心，清风知温柔",
    "花开不语，岁月皆安",
    "与草木相逢，与美好同行",
    "草木皆清欢，风月自安然",
    "一花藏山海，一叶纳春秋",
    "把花草温柔，私藏于时光里",
    "花开有时，岁月无恙",
    "草木无言，自有芳华",
)

fun pickRandomBencaoShareSlogan(): String =
    BencaoSharePosterSlogans.random()

const val SharePosterTargetWidth = 1080
const val SharePosterTargetHeight = 1920

/** 铺满画布 center crop */
fun drawBitmapCover(
    canvas: Canvas,
    bitmap: Bitmap,
    dstW: Int,
    dstH: Int,
    biasSourceX: Float = -0.055f,
    biasSourceY: Float = -0.04f
) {
    val srcW = bitmap.width
    val srcH = bitmap.height
    val srcRatio = srcW.toFloat() / srcH
    val dstRatio = dstW.toFloat() / dstH
    val srcRect = if (srcRatio > dstRatio) {
        val cropW = (srcH * dstRatio).roundToInt().coerceAtLeast(1).coerceAtMost(srcW)
        val dx = (biasSourceX * srcW).roundToInt()
        var left = (srcW - cropW) / 2 + dx
        left = left.coerceIn(0, srcW - cropW)
        Rect(left, 0, left + cropW, srcH)
    } else {
        val cropH = (srcW / dstRatio).roundToInt().coerceAtLeast(1).coerceAtMost(srcH)
        val dy = (biasSourceY * srcH).roundToInt()
        var top = (srcH - cropH) / 2 + dy
        top = top.coerceIn(0, srcH - cropH)
        Rect(0, top, srcW, top + cropH)
    }
    val dstRect = Rect(0, 0, dstW, dstH)
    val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
}

fun drawHermesOrangeBackdrop(canvas: Canvas, w: Int, h: Int, seed: Long) {
    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                android.graphics.Color.parseColor("#FF9A28"),
                android.graphics.Color.parseColor("#FF8200"),
                android.graphics.Color.parseColor("#E85D00"),
                android.graphics.Color.parseColor("#C14400")
            ),
            floatArrayOf(0f, 0.35f, 0.72f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

    val rnd = Random(seed xor 0x9E3779B97F4A7C15uL.toLong())
    repeat(38) {
        val cx = rnd.nextFloat() * w
        val cy = rnd.nextFloat() * h
        val rad = 24f + rnd.nextFloat() * 140f
        val a = 14 + rnd.nextInt(38)
        val blob = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, rad,
                intArrayOf(
                    android.graphics.Color.argb(a, 255, 255, 255),
                    android.graphics.Color.argb(0, 255, 245, 220)
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, rad, blob)
    }

    val streak = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 72f
        strokeCap = Paint.Cap.ROUND
        color = android.graphics.Color.argb(42, 255, 235, 190)
    }
    val arc = Path().apply {
        moveTo(w * 1.08f, h * 0.97f)
        quadTo(w * 0.62f, h * 0.72f, w * 0.28f, h * 0.44f)
    }
    canvas.drawPath(arc, streak)

    val streakHi = Paint(streak).apply {
        strokeWidth = 22f
        color = android.graphics.Color.argb(70, 255, 255, 250)
    }
    canvas.drawPath(arc, streakHi)
}

fun drawBencaoRibbonBands(canvas: Canvas, w: Int, h: Int) {
    val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(235, 22, 22, 28)
    }
    val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(230, 248, 248, 252)
        textSize = 26f
        letterSpacing = 0.22f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    val unit = "FANCAO · "

    fun band(angle: Float, pivotX: Float, pivotY: Float, top: Float, thickness: Float) {
        canvas.save()
        canvas.rotate(angle, pivotX, pivotY)
        canvas.drawRect(-w.toFloat(), top, w * 2f, top + thickness, bandPaint)
        var x = -w * 0.5f
        val baseline = top + thickness * 0.72f
        while (x < w * 1.6f) {
            canvas.drawText(unit, x, baseline, tp)
            x += tp.measureText(unit)
        }
        canvas.restore()
    }
    band(-32f, w * 0.2f, h * 0.48f, h * 0.36f, 46f)
    band(24f, w * 0.55f, h * 0.55f, h * 0.62f, 40f)
}

fun drawVerticalCharsColumn(
    canvas: Canvas, column: String,
    x: Float, topY: Float,
    paint: TextPaint,
    extraLineGap: Float = 10f
) {
    var y = topY
    val fm = paint.fontMetrics
    val lineH = paint.textSize + extraLineGap
    var i = 0
    while (i < column.length) {
        val cp = column.codePointAt(i)
        val ch = String(Character.toChars(cp))
        canvas.drawText(ch, x, y - fm.ascent, paint)
        y += lineH
        i += Character.charCount(cp)
    }
}

fun drawHermesScriptLogo(canvas: Canvas, w: Int, h: Int) {
    val script = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        color = android.graphics.Color.parseColor("#C8161E")
        textSize = 62f
        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        textSkewX = -0.14f
    }
    val scriptText = "Fancao"
    val sx = w * 0.06f
    val sy = h * 0.055f + (-script.fontMetrics.ascent)
    canvas.drawText(scriptText, sx, sy, script)

    val sub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        color = android.graphics.Color.argb(235, 255, 255, 255)
        textSize = 19f
        letterSpacing = 0.28f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    val subText = "UNCOVER BEAUTY"
    canvas.drawText(subText, sx, sy + sub.textSize + 18f, sub)
}

fun drawGoldCalendarAccent(canvas: Canvas, dayOfMonth: Int, anchorX: Float, topY: Float) {
    val d = dayOfMonth.coerceIn(1, 31)
    val text = d.toString()
    val size = 158f
    val glow = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        textSize = size
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = android.graphics.Color.argb(160, 255, 248, 200)
        maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
    }
    val fill = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        textSize = size
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        style = Paint.Style.FILL
        shader = LinearGradient(
            0f, topY - size, 0f, topY + size * 0.3f,
            android.graphics.Color.parseColor("#FFFBE6"),
            android.graphics.Color.parseColor("#D4A017"),
            Shader.TileMode.CLAMP
        )
    }
    val stroke = TextPaint(fill).apply {
        shader = null
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = android.graphics.Color.parseColor("#8B6914")
        maskFilter = null
    }
    val tw = glow.measureText(text)
    val x = anchorX - tw / 2f
    canvas.drawText(text, x, topY, glow)
    glow.maskFilter = null
    canvas.drawText(text, x, topY, fill)
    canvas.drawText(text, x, topY, stroke)

    val spark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(230, 255, 255, 255)
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawLine(x + tw + 6f, topY - size * 0.82f, x + tw + 38f, topY - size * 0.82f, spark)

    val dayLbl = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 34f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val tail = "日"
    canvas.drawText(tail, anchorX - dayLbl.measureText(tail) / 2f, topY + size * 0.38f, dayLbl)
}

fun composeBencaoSharePoster(
    photo: Bitmap,
    bencaoName: String,
    slogan: String,
    createdAtMillis: Long
): Bitmap {
    val w = SharePosterTargetWidth
    val h = SharePosterTargetHeight
    val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)

    canvas.drawColor(android.graphics.Color.WHITE)

    val bottomH = ((h * 0.27f) * (2f / 3f)).roundToInt().coerceIn(240, 480)
    val photoH = (h - bottomH).coerceAtLeast(1)

    canvas.save()
    canvas.clipRect(0, 0, w, photoH)
    drawBitmapCover(canvas, photo, w, photoH, biasSourceX = 0f, biasSourceY = -0.02f)
    canvas.restore()

    val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(26, 0, 0, 0)
        strokeWidth = 2f
    }
    canvas.drawLine(0f, photoH.toFloat(), w.toFloat(), photoH.toFloat(), divider)

    val contentPadX = 72
    val contentW = (w - contentPadX * 2).coerceAtLeast(200)

    val s1 = slogan.trim()
    val line1 = if (s1.isNotEmpty()) "$s1 ｜ 繁草" else "繁草"
    val line1Paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        color = android.graphics.Color.parseColor("#0B3D2E")
        textSize = 54f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        letterSpacing = 0.01f
    }
    val line1Layout = StaticLayout.Builder.obtain(line1, 0, line1.length, line1Paint, contentW)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .build()

    val createdStr = if (createdAtMillis > 0L) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(createdAtMillis))
    } else ""
    val line2 = buildList {
        val n = bencaoName.trim()
        if (n.isNotEmpty()) add(n)
        if (createdStr.isNotEmpty()) add(createdStr)
    }.joinToString("  |  ")
    val line2Paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        color = android.graphics.Color.parseColor("#1F6F5B")
        textSize = 30f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        letterSpacing = 0.02f
    }
    val line2Layout = StaticLayout.Builder.obtain(line2, 0, line2.length, line2Paint, contentW)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .build()

    val gap = 20f
    val stackH = line1Layout.height + gap + line2Layout.height
    val areaTop = photoH.toFloat()
    val areaH = (h - photoH).toFloat()
    val yStart = areaTop + (areaH - stackH) / 2f

    canvas.save()
    canvas.translate(contentPadX.toFloat(), yStart)
    line1Layout.draw(canvas)
    canvas.translate(0f, line1Layout.height + gap)
    line2Layout.draw(canvas)
    canvas.restore()

    return out
}
