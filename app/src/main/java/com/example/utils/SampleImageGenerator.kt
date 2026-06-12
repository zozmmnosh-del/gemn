package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.random.Random

object SampleImageGenerator {

    data class SampleImage(
        val nameAr: String,
        val nameEn: String,
        val description: String,
        val watermarkType: String,
        val expectedBounds: RectBounds, // Pre-calculated mockup fallback bounds (0-1000 coordinate scale)
        val bitmap: Bitmap
    )

    data class RectBounds(
        val ymin: Float,
        val xmin: Float,
        val ymax: Float,
        val xmax: Float
    )

    fun getSamples(): List<SampleImage> {
        return listOf(
            createCosmicNebula(),
            createGoldenSunset(),
            createRobotCore()
        )
    }

    private fun createCosmicNebula(): SampleImage {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // 1. Dark space gradient background
        val bgGradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(Color.parseColor("#0F0C1B"), Color.parseColor("#2C1B4D"), Color.parseColor("#12072B")),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // 2. Cosmic Nebulas (semi-transparent glowing circles)
        val random = Random(42)
        for (i in 0 until 4) {
            val cx = random.nextFloat() * width
            val cy = random.nextFloat() * height
            val radius = 150f + random.nextFloat() * 150f
            val glowColor = when (i % 3) {
                0 -> Color.argb(45, 138, 43, 226)  // Purple
                1 -> Color.argb(35, 0, 191, 255)  // Cyan
                else -> Color.argb(40, 255, 20, 147) // Pink
            }
            val radialGrad = RadialGradient(
                cx, cy, radius,
                intArrayOf(glowColor, Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            paint.shader = radialGrad
            canvas.drawCircle(cx, cy, radius, paint)
        }
        paint.shader = null

        // 3. Glowing Stars
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        for (i in 0 until 60) {
            val sx = random.nextFloat() * width
            val sy = random.nextFloat() * height
            val sa = random.nextInt(100, 255)
            paint.alpha = sa
            val size = 1f + random.nextFloat() * 2.5f
            canvas.drawCircle(sx, sy, size, paint)
        }
        paint.alpha = 255

        // 4. White cross-stars
        for (i in 0 until 5) {
            val sx = random.nextFloat() * width
            val sy = random.nextFloat() * height
            paint.color = Color.WHITE
            canvas.drawLine(sx - 8, sy, sx + 8, sy, paint)
            canvas.drawLine(sx, sy - 8, sx, sy + 8, paint)
        }

        // 5. Drawing the Watermark at bottom right: "Google Imagen 3"
        // Target area: ymin=800, xmin=600, ymax=920, xmax=950 (translated to 800px scale: y=640 to 736, x=480 to 760)
        paint.color = Color.argb(160, 240, 240, 255)
        paint.textSize = 24f
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.RIGHT

        // Draw a simple modern Google-like logo placeholder
        canvas.drawText("Google Imagen 3", width - 40f, height - 70f, paint)

        paint.textSize = 14f
        paint.color = Color.argb(100, 200, 200, 220)
        canvas.drawText("AI GENERATED - SECURE DIGITAL MARK", width - 40f, height - 42f, paint)

        return SampleImage(
            nameAr = "سديم الكون البعيد",
            nameEn = "Deep Cosmic Nebula",
            description = "ألوان غاز الهيدروجين والنجوم اللامعة في أعماق الفضاء مع علامة Google Imagen المائية.",
            watermarkType = "Google Imagen 3 (Bottom-Right)",
            expectedBounds = RectBounds(760f, 560f, 950f, 960f), // in 0-1000 normalized coordinates
            bitmap = bitmap
        )
    }

    private fun createGoldenSunset(): SampleImage {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // 1. Warm Golden/Orange Sunset Gradient background
        val bgGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.parseColor("#4A0E17"), Color.parseColor("#C73E1D"), Color.parseColor("#F18F01"), Color.parseColor("#FFC857")),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // 2. Giant sun glowing
        val sunColorGlow = RadialGradient(
            width * 0.5f, height * 0.65f, 180f,
            intArrayOf(Color.argb(230, 255, 245, 200), Color.argb(80, 255, 200, 50), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = sunColorGlow
        canvas.drawCircle(width * 0.5f, height * 0.65f, 180f, paint)
        paint.shader = null

        // 3. Mountain silhouette lines
        paint.color = Color.parseColor("#23040C")
        paint.style = Paint.Style.FILL
        val path1 = Path()
        path1.moveTo(0f, height.toFloat())
        path1.lineTo(0f, height * 0.7f)
        path1.lineTo(width * 0.35f, height * 0.58f)
        path1.lineTo(width * 0.65f, height * 0.72f)
        path1.lineTo(width.toFloat(), height * 0.62f)
        path1.lineTo(width.toFloat(), height.toFloat())
        path1.close()
        canvas.drawPath(path1, paint)

        // Mountain 2 (foreground)
        paint.color = Color.parseColor("#110004")
        val path2 = Path()
        path2.moveTo(0f, height.toFloat())
        path2.lineTo(0f, height * 0.82f)
        path2.lineTo(width * 0.5f, height * 0.7f)
        path2.lineTo(width.toFloat(), height * 0.85f)
        path2.lineTo(width.toFloat(), height.toFloat())
        path2.close()
        canvas.drawPath(path2, paint)

        // 4. Draw transparent scripted watermark: "Generated by Gemini" on bottom left
        // Target area: ymin=820, xmin=50, ymax=900, xmax=450 (translated to 800px scale: y=656 to 720, x=40 to 360)
        paint.color = Color.argb(120, 255, 255, 255)
        paint.textSize = 26f
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL

        canvas.drawText("Generated by Gemini", 45f, height - 70f, paint)

        paint.textSize = 13f
        paint.color = Color.argb(75, 230, 230, 235)
        canvas.drawText("CREATIVE STUDIO IMAGEN DIGITAL ID: 8904", 45f, height - 44f, paint)

        return SampleImage(
            nameAr = "شروق جيميناي الذهبي",
            nameEn = "Gemini Golden Hour",
            description = "غروب شمس دافئ ورائع بظلال وتدرجات جبلية خلابة مع توقيع جيميناي الشفاف بالأسفل.",
            watermarkType = "Gemini Logo (Bottom-Left)",
            expectedBounds = RectBounds(770f, 30f, 960f, 500f), // in 0-1000 normalized coordinates
            bitmap = bitmap
        )
    }

    private fun createRobotCore(): SampleImage {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // 1. Dark Technical Grid background
        canvas.drawColor(Color.parseColor("#080D15"))
        paint.color = Color.parseColor("#152030")
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE

        val gridSize = 40
        for (x in 0..width step gridSize) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
        }
        for (y in 0..height step gridSize) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        }

        // 2. Cybernetic Concentric Rings
        paint.color = Color.parseColor("#00E5FF")
        paint.strokeWidth = 2f
        canvas.drawCircle(width * 0.5f, height * 0.45f, 120f, paint)

        paint.color = Color.argb(100, 218, 112, 214) // Orchid
        canvas.drawCircle(width * 0.5f, height * 0.45f, 180f, paint)

        // Draw HUD lines
        paint.color = Color.parseColor("#00E5FF")
        canvas.drawLine(width * 0.5f - 240, height * 0.45f, width * 0.5f + 240, height * 0.45f, paint)
        canvas.drawLine(width * 0.5f, height * 0.45f - 240, width * 0.5f, height * 0.45f + 240, paint)

        // Light glow center
        val centerGlow = RadialGradient(
            width * 0.5f, height * 0.45f, 80f,
            intArrayOf(Color.argb(180, 0, 229, 255), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = centerGlow
        canvas.drawCircle(width * 0.5f, height * 0.45f, 80f, paint)
        paint.shader = null

        // 3. Cyber stamp watermarked bounding box: "SynthID ENCRYPTED STAMP"
        // Target area: ymin=750, xmin=250, ymax=880, xmax=750 (translated to 800px scale: y=600 to 704, x=200 to 600)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(30, 0, 229, 255)
        canvas.drawRect(200f, 600f, 600f, 700f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.argb(180, 0, 229, 255)
        paint.strokeWidth = 2f
        canvas.drawRect(200f, 600f, 600f, 700f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 20f
        paint.color = Color.parseColor("#00E5FF")
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("- PATENTED SynthID TECHNOLOGY -", width * 0.5f, 638f, paint)

        paint.textSize = 14f
        paint.color = Color.argb(150, 0, 229, 255)
        canvas.drawText("AI IMAGE INTEGRITY WATERMARK #7701", width * 0.5f, 672f, paint)

        return SampleImage(
            nameAr = "نواة الذكاء الفائق",
            nameEn = "Cortex Cyber Core",
            description = "تكوين خطي هندسي سيبراني مع علامة أمنية مدمجة مشفرة بتقنية SynthID في الوسط.",
            watermarkType = "SynthID Centered ID Stamp",
            expectedBounds = RectBounds(720f, 220f, 900f, 780f), // in 0-1000 normalized coordinates
            bitmap = bitmap
        )
    }
}
