package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.text.normalize

/** Where a regional incident snapshot is in its life cycle. */
enum class RegionalIncidentSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional incident snapshot and its current status. */
data class RegionalIncidentSnapshot(
    var code: String,
    var name: String,
    var carrier: String,
    var ownerId: String,
    var maximum: Double,
    var source: String,
    var status: RegionalIncidentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentSnapshotStatus): Boolean {
        when (status) {
            RegionalIncidentSnapshotStatus.OPEN -> return next == RegionalIncidentSnapshotStatus.IN_REVIEW || next == RegionalIncidentSnapshotStatus.RESOLVED
            RegionalIncidentSnapshotStatus.IN_REVIEW -> return next == RegionalIncidentSnapshotStatus.RESOLVED || next == RegionalIncidentSnapshotStatus.REOPENED
            RegionalIncidentSnapshotStatus.RESOLVED -> return next == RegionalIncidentSnapshotStatus.REOPENED || next == RegionalIncidentSnapshotStatus.ARCHIVED
            RegionalIncidentSnapshotStatus.REOPENED -> {
                when (next) {
                    RegionalIncidentSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIncidentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional incident snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional incident snapshot"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): RegionalIncidentSnapshot {
        return RegionalIncidentSnapshot(
            code = code,
            name = name,
            carrier = carrier,
            ownerId = ownerId,
            maximum = maximum,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional incident snapshot in status open. */
fun newRegionalIncidentSnapshot(code: String, name: String): RegionalIncidentSnapshot {
    return RegionalIncidentSnapshot(
        code = code,
        name = name,
        carrier = "",
        ownerId = "",
        maximum = 0.0,
        source = "",
        status = RegionalIncidentSnapshotStatus.OPEN,
    )
}
