package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched roster cycle is in its life cycle. */
enum class BatchedRosterCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched roster cycle and its current status. */
data class BatchedRosterCycle(
    var code: String,
    var label: String,
    var graceDays: Int,
    var vehicleId: String,
    var etaMinutes: Int,
    var odometer: Int,
    var contactEmail: String,
    var title: String,
    var status: BatchedRosterCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterCycleStatus): Boolean {
        when (status) {
            BatchedRosterCycleStatus.PROPOSED -> return next == BatchedRosterCycleStatus.APPROVED
            BatchedRosterCycleStatus.APPROVED -> {
                when (next) {
                    BatchedRosterCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedRosterCycleStatus.REJECTED -> {
                when (next) {
                    BatchedRosterCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRosterCycleStatus) {
        if (!canMoveTo(next)) {
            error("batched roster cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched roster cycle"} $code"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): BatchedRosterCycle {
        return BatchedRosterCycle(
            code = code,
            label = label,
            graceDays = graceDays,
            vehicleId = vehicleId,
            etaMinutes = etaMinutes,
            odometer = odometer,
            contactEmail = contactEmail,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched roster cycle in status proposed. */
fun newBatchedRosterCycle(code: String, label: String): BatchedRosterCycle {
    return BatchedRosterCycle(
        code = code,
        label = label,
        graceDays = 0,
        vehicleId = "",
        etaMinutes = 0,
        odometer = 0,
        contactEmail = "",
        title = "",
        status = BatchedRosterCycleStatus.PROPOSED,
    )
}
