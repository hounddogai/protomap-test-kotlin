package com.acme.transport.crewing.qualification.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global qualification variance records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a global qualification variance is in its life cycle. */
enum class GlobalQualificationVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global qualification variance as the qualification team keeps it. */
data class GlobalQualificationVariance(
    var code: String,
    var label: String,
    var approved: Boolean,
    var pinned: Boolean,
    var capacity: Int,
    var durationMinutes: Int,
    var minimum: Double,
    var driverId: String,
    var active: Boolean,
    var rate: Double,
    var status: GlobalQualificationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationVarianceStatus): Boolean {
        when (status) {
            GlobalQualificationVarianceStatus.IDLE -> {
                when (next) {
                    GlobalQualificationVarianceStatus.WARMING, GlobalQualificationVarianceStatus.READY -> return true
                }
                return false
            }
            GlobalQualificationVarianceStatus.WARMING -> {
                when (next) {
                    GlobalQualificationVarianceStatus.READY, GlobalQualificationVarianceStatus.DRAINING -> return true
                }
                return false
            }
            GlobalQualificationVarianceStatus.READY -> return next == GlobalQualificationVarianceStatus.DRAINING || next == GlobalQualificationVarianceStatus.STOPPED
            GlobalQualificationVarianceStatus.DRAINING -> return next == GlobalQualificationVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalQualificationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("global qualification variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round(minimum * rate * 100) / 100

    fun snapshot(): GlobalQualificationVariance {
        return GlobalQualificationVariance(
            code = code,
            label = label,
            approved = approved,
            pinned = pinned,
            capacity = capacity,
            durationMinutes = durationMinutes,
            minimum = minimum,
            driverId = driverId,
            active = active,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global qualification variance in status idle. */
fun newGlobalQualificationVariance(code: String, label: String): GlobalQualificationVariance {
    return GlobalQualificationVariance(
        code = code,
        label = label,
        approved = false,
        pinned = false,
        capacity = 0,
        durationMinutes = 0,
        minimum = 0.0,
        driverId = "",
        active = false,
        rate = 0.0,
        status = GlobalQualificationVarianceStatus.IDLE,
    )
}
