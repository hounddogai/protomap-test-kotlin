package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default duty period snapshot is in its life cycle. */
enum class DefaultDutyPeriodSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default duty period snapshot and its current status. */
data class DefaultDutyPeriodSnapshot(
    var code: String,
    var label: String,
    var ceiling: Double,
    var loadKg: Double,
    var updatedAt: Int,
    var budget: Double,
    var timezone: String,
    var depot: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var unit: String,
    var status: DefaultDutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDutyPeriodSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDutyPeriodSnapshotStatus): Boolean {
        when (status) {
            DefaultDutyPeriodSnapshotStatus.REQUESTED -> {
                when (next) {
                    DefaultDutyPeriodSnapshotStatus.REVOKED -> return true
                }
                return false
            }
            DefaultDutyPeriodSnapshotStatus.GRANTED -> return next == DefaultDutyPeriodSnapshotStatus.REVOKED || next == DefaultDutyPeriodSnapshotStatus.EXPIRED
            DefaultDutyPeriodSnapshotStatus.REVOKED -> {
                when (next) {
                    DefaultDutyPeriodSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default duty period snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round(updatedAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): DefaultDutyPeriodSnapshot {
        return DefaultDutyPeriodSnapshot(
            code = code,
            label = label,
            ceiling = ceiling,
            loadKg = loadKg,
            updatedAt = updatedAt,
            budget = budget,
            timezone = timezone,
            depot = depot,
            tags = tags,
            pallets = pallets,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default duty period snapshot in status requested. */
fun newDefaultDutyPeriodSnapshot(code: String, label: String): DefaultDutyPeriodSnapshot {
    return DefaultDutyPeriodSnapshot(
        code = code,
        label = label,
        ceiling = 0.0,
        loadKg = 0.0,
        updatedAt = 0,
        budget = 0.0,
        timezone = "",
        depot = "",
        tags = mutableListOf<String>(),
        pallets = 0,
        unit = "",
        status = DefaultDutyPeriodSnapshotStatus.REQUESTED,
    )
}
