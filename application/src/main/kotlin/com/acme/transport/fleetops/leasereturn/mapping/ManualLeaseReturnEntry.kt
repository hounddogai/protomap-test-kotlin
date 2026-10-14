package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual lease return entry is in its life cycle. */
enum class ManualLeaseReturnEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One manual lease return entry, with what the planners need to act on it. */
data class ManualLeaseReturnEntry(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var trailerId: String,
    var amount: Double,
    var status: ManualLeaseReturnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLeaseReturnEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLeaseReturnEntryStatus): Boolean {
        when (status) {
            ManualLeaseReturnEntryStatus.NEW -> return next == ManualLeaseReturnEntryStatus.QUEUED || next == ManualLeaseReturnEntryStatus.DONE
            ManualLeaseReturnEntryStatus.QUEUED -> return next == ManualLeaseReturnEntryStatus.DONE
            ManualLeaseReturnEntryStatus.RUNNING -> {
                when (next) {
                    ManualLeaseReturnEntryStatus.DONE, ManualLeaseReturnEntryStatus.FAILED -> return true
                }
                return false
            }
            ManualLeaseReturnEntryStatus.DONE -> {
                when (next) {
                    ManualLeaseReturnEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualLeaseReturnEntryStatus) {
        if (!canMoveTo(next)) {
            error("manual lease return entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round((fuelLiters + amount) * 100) / 100

    fun snapshot(): ManualLeaseReturnEntry {
        return ManualLeaseReturnEntry(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual lease return entry in status new. */
fun newManualLeaseReturnEntry(code: String, label: String): ManualLeaseReturnEntry {
    return ManualLeaseReturnEntry(
        code = code,
        label = label,
        fuelLiters = 0.0,
        trailerId = "",
        amount = 0.0,
        status = ManualLeaseReturnEntryStatus.NEW,
    )
}
