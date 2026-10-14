package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered trailer window is in its life cycle. */
enum class TieredTrailerWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered trailer window. Its code is unique within a tenant. */
data class TieredTrailerWindow(
    var code: String,
    var label: String,
    var threshold: Double,
    var updatedAt: Int,
    var currency: String,
    var shiftCode: String,
    var visible: Boolean,
    var status: TieredTrailerWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTrailerWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTrailerWindowStatus): Boolean {
        when (status) {
            TieredTrailerWindowStatus.IDLE -> return next == TieredTrailerWindowStatus.STOPPED
            TieredTrailerWindowStatus.WARMING -> return next == TieredTrailerWindowStatus.STOPPED
            TieredTrailerWindowStatus.READY -> {
                when (next) {
                    TieredTrailerWindowStatus.DRAINING, TieredTrailerWindowStatus.STOPPED -> return true
                }
                return false
            }
            TieredTrailerWindowStatus.DRAINING -> {
                when (next) {
                    TieredTrailerWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTrailerWindowStatus) {
        if (!canMoveTo(next)) {
            error("tiered trailer window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered trailer window"} $code"

    fun adjustedExposure(): Double = round((updatedAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): TieredTrailerWindow {
        return TieredTrailerWindow(
            code = code,
            label = label,
            threshold = threshold,
            updatedAt = updatedAt,
            currency = currency,
            shiftCode = shiftCode,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered trailer window in status idle. */
fun newTieredTrailerWindow(code: String, label: String): TieredTrailerWindow {
    return TieredTrailerWindow(
        code = code,
        label = label,
        threshold = 0.0,
        updatedAt = 0,
        currency = "",
        shiftCode = "",
        visible = false,
        status = TieredTrailerWindowStatus.IDLE,
    )
}
