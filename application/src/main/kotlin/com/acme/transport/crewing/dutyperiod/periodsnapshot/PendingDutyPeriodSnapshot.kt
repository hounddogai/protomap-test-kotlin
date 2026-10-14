package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending duty period snapshot records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a pending duty period snapshot is in its life cycle. */
enum class PendingDutyPeriodSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending duty period snapshot as the duty period team keeps it. */
data class PendingDutyPeriodSnapshot(
    var code: String,
    var label: String,
    var driverId: String,
    var endsAt: Int,
    var pallets: Int,
    var createdAt: Int,
    var rate: Double,
    var locale: String,
    var version: Int,
    var status: PendingDutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodSnapshotStatus): Boolean {
        when (status) {
            PendingDutyPeriodSnapshotStatus.DRAFT -> {
                when (next) {
                    PendingDutyPeriodSnapshotStatus.PENDING, PendingDutyPeriodSnapshotStatus.ACTIVE -> return true
                }
                return false
            }
            PendingDutyPeriodSnapshotStatus.PENDING -> return next == PendingDutyPeriodSnapshotStatus.ACTIVE || next == PendingDutyPeriodSnapshotStatus.CLOSED
            PendingDutyPeriodSnapshotStatus.ACTIVE -> {
                when (next) {
                    PendingDutyPeriodSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingDutyPeriodSnapshotStatus.SUSPENDED -> {
                when (next) {
                    PendingDutyPeriodSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"pending duty period snapshot"} $code"

    fun netVolume(): Double = round(pallets.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): PendingDutyPeriodSnapshot {
        return PendingDutyPeriodSnapshot(
            code = code,
            label = label,
            driverId = driverId,
            endsAt = endsAt,
            pallets = pallets,
            createdAt = createdAt,
            rate = rate,
            locale = locale,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending duty period snapshot in status draft. */
fun newPendingDutyPeriodSnapshot(code: String, label: String): PendingDutyPeriodSnapshot {
    return PendingDutyPeriodSnapshot(
        code = code,
        label = label,
        driverId = "",
        endsAt = 0,
        pallets = 0,
        createdAt = 0,
        rate = 0.0,
        locale = "",
        version = 0,
        status = PendingDutyPeriodSnapshotStatus.DRAFT,
    )
}
