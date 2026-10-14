package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.text.normalize

// The primary ev charge segment records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a primary ev charge segment is in its life cycle. */
enum class PrimaryEvChargeSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary ev charge segment as the ev charge team keeps it. */
data class PrimaryEvChargeSegment(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var driverId: String,
    var dock: String,
    var status: PrimaryEvChargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeSegmentStatus): Boolean {
        when (status) {
            PrimaryEvChargeSegmentStatus.IDLE -> return next == PrimaryEvChargeSegmentStatus.WARMING || next == PrimaryEvChargeSegmentStatus.STOPPED
            PrimaryEvChargeSegmentStatus.WARMING -> {
                when (next) {
                    PrimaryEvChargeSegmentStatus.READY, PrimaryEvChargeSegmentStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryEvChargeSegmentStatus.READY -> return next == PrimaryEvChargeSegmentStatus.DRAINING
            PrimaryEvChargeSegmentStatus.DRAINING -> return next == PrimaryEvChargeSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): PrimaryEvChargeSegment {
        return PrimaryEvChargeSegment(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            driverId = driverId,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary ev charge segment in status idle. */
fun newPrimaryEvChargeSegment(code: String, label: String): PrimaryEvChargeSegment {
    return PrimaryEvChargeSegment(
        code = code,
        label = label,
        etaMinutes = 0,
        driverId = "",
        dock = "",
        status = PrimaryEvChargeSegmentStatus.IDLE,
    )
}
