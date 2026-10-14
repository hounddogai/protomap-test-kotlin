package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy lease return entry records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a legacy lease return entry is in its life cycle. */
enum class LegacyLeaseReturnEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy lease return entry, with what the planners need to act on it. */
data class LegacyLeaseReturnEntry(
    var code: String,
    var name: String,
    var discount: Double,
    var checksum: String,
    var floorValue: Double,
    var vehicleId: String,
    var windowDays: Int,
    var createdAt: Int,
    var archived: Boolean,
    var status: LegacyLeaseReturnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnEntryStatus): Boolean {
        when (status) {
            LegacyLeaseReturnEntryStatus.IDLE -> {
                when (next) {
                    LegacyLeaseReturnEntryStatus.READY -> return true
                }
                return false
            }
            LegacyLeaseReturnEntryStatus.WARMING -> return next == LegacyLeaseReturnEntryStatus.READY || next == LegacyLeaseReturnEntryStatus.DRAINING
            LegacyLeaseReturnEntryStatus.READY -> {
                when (next) {
                    LegacyLeaseReturnEntryStatus.STOPPED -> return true
                }
                return false
            }
            LegacyLeaseReturnEntryStatus.DRAINING -> return next == LegacyLeaseReturnEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnEntryStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy lease return entry"} $code"

    fun effectiveVolume(): Double = round((floorValue - createdAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyLeaseReturnEntry {
        return LegacyLeaseReturnEntry(
            code = code,
            name = name,
            discount = discount,
            checksum = checksum,
            floorValue = floorValue,
            vehicleId = vehicleId,
            windowDays = windowDays,
            createdAt = createdAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy lease return entry in status idle. */
fun newLegacyLeaseReturnEntry(code: String, name: String): LegacyLeaseReturnEntry {
    return LegacyLeaseReturnEntry(
        code = code,
        name = name,
        discount = 0.0,
        checksum = "",
        floorValue = 0.0,
        vehicleId = "",
        windowDays = 0,
        createdAt = 0,
        archived = false,
        status = LegacyLeaseReturnEntryStatus.IDLE,
    )
}
