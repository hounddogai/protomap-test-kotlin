package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive cross dock adjustment is in its life cycle. */
enum class AdaptiveCrossDockAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive cross dock adjustment, with what the planners need to act on it. */
data class AdaptiveCrossDockAdjustment(
    var code: String,
    var label: String,
    var graceDays: Int,
    var phone: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var country: String,
    var ratio: Double,
    var priority: Int,
    var status: AdaptiveCrossDockAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCrossDockAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCrossDockAdjustmentStatus): Boolean {
        when (status) {
            AdaptiveCrossDockAdjustmentStatus.OPEN -> {
                when (next) {
                    AdaptiveCrossDockAdjustmentStatus.IN_REVIEW -> return true
                }
                return false
            }
            AdaptiveCrossDockAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveCrossDockAdjustmentStatus.RESOLVED, AdaptiveCrossDockAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveCrossDockAdjustmentStatus.RESOLVED -> return next == AdaptiveCrossDockAdjustmentStatus.REOPENED
            AdaptiveCrossDockAdjustmentStatus.REOPENED -> return next == AdaptiveCrossDockAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveCrossDockAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cross dock adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive cross dock adjustment"} $code"

    fun adjustedExposure(): Double = round(loadKg * graceDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveCrossDockAdjustment {
        return AdaptiveCrossDockAdjustment(
            code = code,
            label = label,
            graceDays = graceDays,
            phone = phone,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            country = country,
            ratio = ratio,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive cross dock adjustment in status open. */
fun newAdaptiveCrossDockAdjustment(code: String, label: String): AdaptiveCrossDockAdjustment {
    return AdaptiveCrossDockAdjustment(
        code = code,
        label = label,
        graceDays = 0,
        phone = "",
        loadKg = 0.0,
        durationMinutes = 0,
        country = "",
        ratio = 0.0,
        priority = 0,
        status = AdaptiveCrossDockAdjustmentStatus.OPEN,
    )
}
