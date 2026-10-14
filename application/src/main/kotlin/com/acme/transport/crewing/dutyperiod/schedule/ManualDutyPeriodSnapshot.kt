package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual duty period snapshot is in its life cycle. */
enum class ManualDutyPeriodSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual duty period snapshot. Its code is unique within a tenant. */
data class ManualDutyPeriodSnapshot(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var distanceKm: Double,
    var capacity: Int,
    var ceiling: Double,
    var priority: Int,
    var retries: Int,
    var active: Boolean,
    var segment: String,
    var status: ManualDutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodSnapshotStatus): Boolean {
        when (status) {
            ManualDutyPeriodSnapshotStatus.PLANNED -> return next == ManualDutyPeriodSnapshotStatus.COMPLETED
            ManualDutyPeriodSnapshotStatus.SCHEDULED -> {
                when (next) {
                    ManualDutyPeriodSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            ManualDutyPeriodSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    ManualDutyPeriodSnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            ManualDutyPeriodSnapshotStatus.COMPLETED -> return next == ManualDutyPeriodSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual duty period snapshot"} $code"

    fun effectiveTotal(): Double = round((capacity.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ManualDutyPeriodSnapshot {
        return ManualDutyPeriodSnapshot(
            code = code,
            name = name,
            expiresAt = expiresAt,
            distanceKm = distanceKm,
            capacity = capacity,
            ceiling = ceiling,
            priority = priority,
            retries = retries,
            active = active,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual duty period snapshot in status planned. */
fun newManualDutyPeriodSnapshot(code: String, name: String): ManualDutyPeriodSnapshot {
    return ManualDutyPeriodSnapshot(
        code = code,
        name = name,
        expiresAt = 0,
        distanceKm = 0.0,
        capacity = 0,
        ceiling = 0.0,
        priority = 0,
        retries = 0,
        active = false,
        segment = "",
        status = ManualDutyPeriodSnapshotStatus.PLANNED,
    )
}
