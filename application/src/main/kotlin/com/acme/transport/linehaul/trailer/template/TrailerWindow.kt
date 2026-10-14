package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer window is in its life cycle. */
enum class TrailerWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One trailer window, with what the planners need to act on it. */
data class TrailerWindow(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var externalRef: String,
    var updatedAt: Int,
    var status: TrailerWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerWindowStatus): Boolean {
        when (status) {
            TrailerWindowStatus.PROPOSED -> {
                when (next) {
                    TrailerWindowStatus.APPROVED, TrailerWindowStatus.REJECTED -> return true
                }
                return false
            }
            TrailerWindowStatus.APPROVED -> {
                when (next) {
                    TrailerWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerWindowStatus.REJECTED -> {
                when (next) {
                    TrailerWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerWindowStatus) {
        if (!canMoveTo(next)) {
            error("trailer window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"trailer window"} $code"

    fun adjustedTotal(): Double = round((fuelLiters - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TrailerWindow {
        return TrailerWindow(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer window in status proposed. */
fun newTrailerWindow(code: String, title: String): TrailerWindow {
    return TrailerWindow(
        code = code,
        title = title,
        fuelLiters = 0.0,
        externalRef = "",
        updatedAt = 0,
        status = TrailerWindowStatus.PROPOSED,
    )
}
