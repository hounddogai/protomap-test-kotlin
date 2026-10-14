package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual clearance notice is in its life cycle. */
enum class ManualClearanceNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual clearance notice as the clearance team keeps it. */
data class ManualClearanceNotice(
    var code: String,
    var label: String,
    var source: String,
    var sequence: Int,
    var version: Int,
    var vehicleId: String,
    var status: ManualClearanceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualClearanceNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualClearanceNoticeStatus): Boolean {
        when (status) {
            ManualClearanceNoticeStatus.IDLE -> return next == ManualClearanceNoticeStatus.READY
            ManualClearanceNoticeStatus.WARMING -> return next == ManualClearanceNoticeStatus.DRAINING
            ManualClearanceNoticeStatus.READY -> {
                when (next) {
                    ManualClearanceNoticeStatus.DRAINING -> return true
                }
                return false
            }
            ManualClearanceNoticeStatus.DRAINING -> return next == ManualClearanceNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualClearanceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("manual clearance notice $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"manual clearance notice"} $code"

    fun adjustedLoad(): Double = round((version.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): ManualClearanceNotice {
        return ManualClearanceNotice(
            code = code,
            label = label,
            source = source,
            sequence = sequence,
            version = version,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual clearance notice in status idle. */
fun newManualClearanceNotice(code: String, label: String): ManualClearanceNotice {
    return ManualClearanceNotice(
        code = code,
        label = label,
        source = "",
        sequence = 0,
        version = 0,
        vehicleId = "",
        status = ManualClearanceNoticeStatus.IDLE,
    )
}
