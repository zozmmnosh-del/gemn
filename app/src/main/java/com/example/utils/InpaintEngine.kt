package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

object InpaintEngine {
    /**
     * Inpaints a bitmap using a fast, high-performance boundary-propagation algorithm
     * restricted specifically to the masked Region of Interest (ROI) for speed.
     *
     * @param source The source image bitmap
     * @param mask A bitmap of the same dimensions where painted/masked pixels have non-zero alpha
     * @return A newly healed and in-painted Bitmap
     */
    fun process(source: Bitmap, mask: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        // Find bounding box (ROI) of the mask to optimize performance
        var minX = width
        var maxX = -1
        var minY = height
        var maxY = -1

        val maskPixels = IntArray(width * height)
        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = maskPixels[y * width + x]
                val alpha = Color.alpha(pixel)
                if (alpha > 30) { // painted pixel
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        // If nothing is masked, return copy of source
        if (maxX == -1 || maxY == -1) {
            return output
        }

        // Add padding around the bounding box to ensure surrounding textures are included
        val padding = 15
        minX = max(0, minX - padding)
        maxX = min(width - 1, maxX + padding)
        minY = max(0, minY - padding)
        maxY = min(height - 1, maxY + padding)

        val roiW = maxX - minX + 1
        val roiH = maxY - minY + 1

        val sourcePixels = IntArray(roiW * roiH)
        val resultPixels = IntArray(roiW * roiH)
        val roiMask = BooleanArray(roiW * roiH)

        // Read pixels within the ROI
        for (y in 0 until roiH) {
            val srcY = minY + y
            for (x in 0 until roiW) {
                val srcX = minX + x
                val origPixel = source.getPixel(srcX, srcY)
                val mPixel = mask.getPixel(srcX, srcY)
                val isMasked = Color.alpha(mPixel) > 30

                val index = y * roiW + x
                sourcePixels[index] = origPixel
                resultPixels[index] = origPixel
                roiMask[index] = isMasked
            }
        }

        val maxRadius = 25
        // Propagate colors from boundaries inwards
        for (y in 0 until roiH) {
            for (x in 0 until roiW) {
                val idx = y * roiW + x
                if (roiMask[idx]) {
                    var sumR = 0L
                    var sumG = 0L
                    var sumB = 0L
                    var count = 0

                    // Search outwards in spirals/rings
                    var found = false
                    for (r in 1..maxRadius) {
                        for (dy in -r..r) {
                            for (dx in -r..r) {
                                if (dx != -r && dx != r && dy != -r && dy != r) continue // only check boundary
                                val nx = x + dx
                                val ny = y + dy
                                if (nx in 0 until roiW && ny in 0 until roiH) {
                                    val nIdx = ny * roiW + nx
                                    if (!roiMask[nIdx]) {
                                        val pixel = sourcePixels[nIdx]
                                        sumR += Color.red(pixel)
                                        sumG += Color.green(pixel)
                                        sumB += Color.blue(pixel)
                                        count++
                                        found = true
                                    }
                                }
                            }
                        }
                        if (found) break
                    }

                    if (count > 0) {
                        resultPixels[idx] = Color.rgb((sumR / count).toInt(), (sumG / count).toInt(), (sumB / count).toInt())
                    }
                }
            }
        }

        // Multi-pass local blur inside mask to smooth colors
        val blurredPixels = resultPixels.clone()
        val blurPasses = 3
        for (pass in 0 until blurPasses) {
            for (y in 1 until roiH - 1) {
                for (x in 1 until roiW - 1) {
                    val idx = y * roiW + x
                    if (roiMask[idx]) {
                        var sumR = 0
                        var sumG = 0
                        var sumB = 0
                        var count = 0

                        for (dy in -1..1) {
                            for (dx in -1..1) {
                                val nIdx = (y + dy) * roiW + (x + dx)
                                val pixel = blurredPixels[nIdx]
                                sumR += Color.red(pixel)
                                sumG += Color.green(pixel)
                                sumB += Color.blue(pixel)
                                count++
                            }
                        }
                        if (count > 0) {
                            resultPixels[idx] = Color.rgb(sumR / count, sumG / count, sumB / count)
                        }
                    }
                }
            }
            System.arraycopy(resultPixels, 0, blurredPixels, 0, resultPixels.size)
        }

        output.setPixels(resultPixels, 0, roiW, minX, minY, roiW, roiH)
        return output
    }
}
