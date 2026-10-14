package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.text.normalize

// The rolling duty period snapshot records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a rolling duty period snapshot is in its life cycle. */
enum class RollingDutyPeriodSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling duty period snapshot. Its code is unique within a tenant. */
data class RollingDutyPeriodSnapshot(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var driverId: String,
    var locale: String,
    var reviewer: String,
    var vehicleId: String,
    var channel: String,
    var timezone: String,
    var score: Double,
    var status: RollingDutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDutyPeriodSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDutyPeriodSnapshotStatus): Boolean {
        when (status) {
            RollingDutyPeriodSnapshotStatus.PLANNED -> return next == RollingDutyPeriodSnapshotStatus.COMPLETED
            RollingDutyPeriodSnapshotStatus.SCHEDULED -> {
                when (next) {
                    RollingDutyPeriodSnapshotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingDutyPeriodSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    RollingDutyPeriodSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDutyPeriodSnapshotStatus.COMPLETED -> return next == RollingDutyPeriodSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingDutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rolling duty period snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling duty period snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): RollingDutyPeriodSnapshot {
        return RollingDutyPeriodSnapshot(
            code = code,
            name = name,
            attributes = attributes,
            driverId = driverId,
            locale = locale,
            reviewer = reviewer,
            vehicleId = vehicleId,
            channel = channel,
            timezone = timezone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling duty period snapshot in status planned. */
fun newRollingDutyPeriodSnapshot(code: String, name: String): RollingDutyPeriodSnapshot {
    return RollingDutyPeriodSnapshot(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        driverId = "",
        locale = "",
        reviewer = "",
        vehicleId = "",
        channel = "",
        timezone = "",
        score = 0.0,
        status = RollingDutyPeriodSnapshotStatus.PLANNED,
    )
}
