package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy capacity snapshot is in its life cycle. */
enum class LegacyCapacitySnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy capacity snapshot as the capacity team keeps it. */
data class LegacyCapacitySnapshot(
    var code: String,
    var name: String,
    var amount: Double,
    var loadKg: Double,
    var maximum: Double,
    var summary: String,
    var retries: Int,
    var status: LegacyCapacitySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCapacitySnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCapacitySnapshotStatus): Boolean {
        when (status) {
            LegacyCapacitySnapshotStatus.IDLE -> {
                when (next) {
                    LegacyCapacitySnapshotStatus.DRAINING, LegacyCapacitySnapshotStatus.STOPPED -> return true
                }
                return false
            }
            LegacyCapacitySnapshotStatus.WARMING -> {
                when (next) {
                    LegacyCapacitySnapshotStatus.DRAINING, LegacyCapacitySnapshotStatus.STOPPED -> return true
                }
                return false
            }
            LegacyCapacitySnapshotStatus.READY -> return next == LegacyCapacitySnapshotStatus.DRAINING || next == LegacyCapacitySnapshotStatus.STOPPED
            LegacyCapacitySnapshotStatus.DRAINING -> {
                when (next) {
                    LegacyCapacitySnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCapacitySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy capacity snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round(maximum * loadKg * 100) / 100

    fun snapshot(): LegacyCapacitySnapshot {
        return LegacyCapacitySnapshot(
            code = code,
            name = name,
            amount = amount,
            loadKg = loadKg,
            maximum = maximum,
            summary = summary,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy capacity snapshot in status idle. */
fun newLegacyCapacitySnapshot(code: String, name: String): LegacyCapacitySnapshot {
    return LegacyCapacitySnapshot(
        code = code,
        name = name,
        amount = 0.0,
        loadKg = 0.0,
        maximum = 0.0,
        summary = "",
        retries = 0,
        status = LegacyCapacitySnapshotStatus.IDLE,
    )
}
