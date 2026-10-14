package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period snapshot records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period snapshot is in its life cycle. */
enum class DutyPeriodSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One duty period snapshot, with what the planners need to act on it. */
data class DutyPeriodSnapshot(
    var code: String,
    var title: String,
    var pallets: Int,
    var trailerId: String,
    var weight: Double,
    var approved: Boolean,
    var contactEmail: String,
    var retries: Int,
    var summary: String,
    var vehicleId: String,
    var durationMinutes: Int,
    var status: DutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodSnapshotStatus): Boolean {
        when (status) {
            DutyPeriodSnapshotStatus.PROPOSED -> {
                when (next) {
                    DutyPeriodSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            DutyPeriodSnapshotStatus.APPROVED -> {
                when (next) {
                    DutyPeriodSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodSnapshotStatus.REJECTED -> {
                when (next) {
                    DutyPeriodSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("duty period snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round(retries.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodSnapshot {
        return DutyPeriodSnapshot(
            code = code,
            title = title,
            pallets = pallets,
            trailerId = trailerId,
            weight = weight,
            approved = approved,
            contactEmail = contactEmail,
            retries = retries,
            summary = summary,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period snapshot in status proposed. */
fun newDutyPeriodSnapshot(code: String, title: String): DutyPeriodSnapshot {
    return DutyPeriodSnapshot(
        code = code,
        title = title,
        pallets = 0,
        trailerId = "",
        weight = 0.0,
        approved = false,
        contactEmail = "",
        retries = 0,
        summary = "",
        vehicleId = "",
        durationMinutes = 0,
        status = DutyPeriodSnapshotStatus.PROPOSED,
    )
}
