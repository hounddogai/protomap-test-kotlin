package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual accessorial adjustment records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a manual accessorial adjustment is in its life cycle. */
enum class ManualAccessorialAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One manual accessorial adjustment, with what the planners need to act on it. */
data class ManualAccessorialAdjustment(
    var code: String,
    var title: String,
    var capacity: Int,
    var limit: Int,
    var score: Double,
    var version: Int,
    var shiftCode: String,
    var status: ManualAccessorialAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAccessorialAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAccessorialAdjustmentStatus): Boolean {
        when (status) {
            ManualAccessorialAdjustmentStatus.IDLE -> {
                when (next) {
                    ManualAccessorialAdjustmentStatus.READY, ManualAccessorialAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            ManualAccessorialAdjustmentStatus.WARMING -> return next == ManualAccessorialAdjustmentStatus.READY || next == ManualAccessorialAdjustmentStatus.DRAINING
            ManualAccessorialAdjustmentStatus.READY -> return next == ManualAccessorialAdjustmentStatus.DRAINING || next == ManualAccessorialAdjustmentStatus.STOPPED
            ManualAccessorialAdjustmentStatus.DRAINING -> {
                when (next) {
                    ManualAccessorialAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAccessorialAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("manual accessorial adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round(limit.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): ManualAccessorialAdjustment {
        return ManualAccessorialAdjustment(
            code = code,
            title = title,
            capacity = capacity,
            limit = limit,
            score = score,
            version = version,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual accessorial adjustment in status idle. */
fun newManualAccessorialAdjustment(code: String, title: String): ManualAccessorialAdjustment {
    return ManualAccessorialAdjustment(
        code = code,
        title = title,
        capacity = 0,
        limit = 0,
        score = 0.0,
        version = 0,
        shiftCode = "",
        status = ManualAccessorialAdjustmentStatus.IDLE,
    )
}
