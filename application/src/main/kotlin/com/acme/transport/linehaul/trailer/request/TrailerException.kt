package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer exception is in its life cycle. */
enum class TrailerExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer exception as the trailer team keeps it. */
data class TrailerException(
    var code: String,
    var label: String,
    var stopCount: Int,
    var title: String,
    var minimum: Double,
    var status: TrailerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerExceptionStatus): Boolean {
        when (status) {
            TrailerExceptionStatus.PROPOSED -> {
                when (next) {
                    TrailerExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerExceptionStatus.APPROVED -> {
                when (next) {
                    TrailerExceptionStatus.REJECTED, TrailerExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerExceptionStatus.REJECTED -> return next == TrailerExceptionStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TrailerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("trailer exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((stopCount.toDouble() + minimum) * 100) / 100

    fun snapshot(): TrailerException {
        return TrailerException(
            code = code,
            label = label,
            stopCount = stopCount,
            title = title,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer exception in status proposed. */
fun newTrailerException(code: String, label: String): TrailerException {
    return TrailerException(
        code = code,
        label = label,
        stopCount = 0,
        title = "",
        minimum = 0.0,
        status = TrailerExceptionStatus.PROPOSED,
    )
}
