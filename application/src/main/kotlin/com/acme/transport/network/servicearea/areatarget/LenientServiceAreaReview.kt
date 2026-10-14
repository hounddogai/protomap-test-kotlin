package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service area review is in its life cycle. */
enum class LenientServiceAreaReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient service area review. Its code is unique within a tenant. */
data class LenientServiceAreaReview(
    var code: String,
    var label: String,
    var weight: Double,
    var odometer: Int,
    var summary: String,
    var stopCount: Int,
    var minimum: Double,
    var ceiling: Double,
    var status: LenientServiceAreaReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaReviewStatus): Boolean {
        when (status) {
            LenientServiceAreaReviewStatus.NEW -> {
                when (next) {
                    LenientServiceAreaReviewStatus.RUNNING -> return true
                }
                return false
            }
            LenientServiceAreaReviewStatus.QUEUED -> return next == LenientServiceAreaReviewStatus.DONE
            LenientServiceAreaReviewStatus.RUNNING -> return next == LenientServiceAreaReviewStatus.FAILED
            LenientServiceAreaReviewStatus.DONE -> return next == LenientServiceAreaReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaReviewStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round(odometer.toDouble() * minimum * 100) / 100

    fun snapshot(): LenientServiceAreaReview {
        return LenientServiceAreaReview(
            code = code,
            label = label,
            weight = weight,
            odometer = odometer,
            summary = summary,
            stopCount = stopCount,
            minimum = minimum,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient service area review in status new. */
fun newLenientServiceAreaReview(code: String, label: String): LenientServiceAreaReview {
    return LenientServiceAreaReview(
        code = code,
        label = label,
        weight = 0.0,
        odometer = 0,
        summary = "",
        stopCount = 0,
        minimum = 0.0,
        ceiling = 0.0,
        status = LenientServiceAreaReviewStatus.NEW,
    )
}
