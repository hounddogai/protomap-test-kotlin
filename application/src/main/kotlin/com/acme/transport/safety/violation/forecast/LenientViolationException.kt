package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient violation exception is in its life cycle. */
enum class LenientViolationExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient violation exception and its current status. */
data class LenientViolationException(
    var code: String,
    var title: String,
    var attempts: Int,
    var archived: Boolean,
    var ratio: Double,
    var floorValue: Double,
    var timezone: String,
    var stopCount: Int,
    var threshold: Double,
    var status: LenientViolationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientViolationExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientViolationExceptionStatus): Boolean {
        when (status) {
            LenientViolationExceptionStatus.IDLE -> return next == LenientViolationExceptionStatus.DRAINING
            LenientViolationExceptionStatus.WARMING -> {
                when (next) {
                    LenientViolationExceptionStatus.DRAINING, LenientViolationExceptionStatus.STOPPED -> return true
                }
                return false
            }
            LenientViolationExceptionStatus.READY -> return next == LenientViolationExceptionStatus.DRAINING || next == LenientViolationExceptionStatus.STOPPED
            LenientViolationExceptionStatus.DRAINING -> return next == LenientViolationExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientViolationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("lenient violation exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round((floorValue - attempts.toDouble()) * 100) / 100

    fun snapshot(): LenientViolationException {
        return LenientViolationException(
            code = code,
            title = title,
            attempts = attempts,
            archived = archived,
            ratio = ratio,
            floorValue = floorValue,
            timezone = timezone,
            stopCount = stopCount,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient violation exception in status idle. */
fun newLenientViolationException(code: String, title: String): LenientViolationException {
    return LenientViolationException(
        code = code,
        title = title,
        attempts = 0,
        archived = false,
        ratio = 0.0,
        floorValue = 0.0,
        timezone = "",
        stopCount = 0,
        threshold = 0.0,
        status = LenientViolationExceptionStatus.IDLE,
    )
}
