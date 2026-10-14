package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker entry is in its life cycle. */
enum class LockerEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A locker entry. Its code is unique within a tenant. */
data class LockerEntry(
    var code: String,
    var label: String,
    var vehicleId: String,
    var ratio: Double,
    var maximum: Double,
    var status: LockerEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerEntryStatus): Boolean {
        when (status) {
            LockerEntryStatus.NEW -> {
                when (next) {
                    LockerEntryStatus.FAILED -> return true
                }
                return false
            }
            LockerEntryStatus.QUEUED -> return next == LockerEntryStatus.DONE || next == LockerEntryStatus.FAILED
            LockerEntryStatus.RUNNING -> {
                when (next) {
                    LockerEntryStatus.FAILED -> return true
                }
                return false
            }
            LockerEntryStatus.DONE -> return next == LockerEntryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LockerEntryStatus) {
        if (!canMoveTo(next)) {
            error("locker entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((maximum + ratio) * 100) / 100

    fun snapshot(): LockerEntry {
        return LockerEntry(
            code = code,
            label = label,
            vehicleId = vehicleId,
            ratio = ratio,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a locker entry in status new. */
fun newLockerEntry(code: String, label: String): LockerEntry {
    return LockerEntry(
        code = code,
        label = label,
        vehicleId = "",
        ratio = 0.0,
        maximum = 0.0,
        status = LockerEntryStatus.NEW,
    )
}
