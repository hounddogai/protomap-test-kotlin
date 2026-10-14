package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual utilization record records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a manual utilization record is in its life cycle. */
enum class ManualUtilizationRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual utilization record, with what the planners need to act on it. */
data class ManualUtilizationRecord(
    var code: String,
    var label: String,
    var maximum: Double,
    var driverId: String,
    var stopCount: Int,
    var odometer: Int,
    var startsAt: Int,
    var attempts: Int,
    var status: ManualUtilizationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationRecordStatus): Boolean {
        when (status) {
            ManualUtilizationRecordStatus.PROPOSED -> {
                when (next) {
                    ManualUtilizationRecordStatus.APPROVED -> return true
                }
                return false
            }
            ManualUtilizationRecordStatus.APPROVED -> {
                when (next) {
                    ManualUtilizationRecordStatus.REJECTED -> return true
                }
                return false
            }
            ManualUtilizationRecordStatus.REJECTED -> return next == ManualUtilizationRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualUtilizationRecordStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round(attempts.toDouble() * maximum * 100) / 100

    fun snapshot(): ManualUtilizationRecord {
        return ManualUtilizationRecord(
            code = code,
            label = label,
            maximum = maximum,
            driverId = driverId,
            stopCount = stopCount,
            odometer = odometer,
            startsAt = startsAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual utilization record in status proposed. */
fun newManualUtilizationRecord(code: String, label: String): ManualUtilizationRecord {
    return ManualUtilizationRecord(
        code = code,
        label = label,
        maximum = 0.0,
        driverId = "",
        stopCount = 0,
        odometer = 0,
        startsAt = 0,
        attempts = 0,
        status = ManualUtilizationRecordStatus.PROPOSED,
    )
}
