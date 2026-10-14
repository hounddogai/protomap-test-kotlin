package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection snapshot is in its life cycle. */
enum class InspectionSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A inspection snapshot as the inspection team keeps it. */
data class InspectionSnapshot(
    var code: String,
    var label: String,
    var carrier: String,
    var updatedAt: Int,
    var description: String,
    var unit: String,
    var etaMinutes: Int,
    var notes: String,
    var status: InspectionSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionSnapshotStatus): Boolean {
        when (status) {
            InspectionSnapshotStatus.OPEN -> {
                when (next) {
                    InspectionSnapshotStatus.IN_REVIEW -> return true
                }
                return false
            }
            InspectionSnapshotStatus.IN_REVIEW -> {
                when (next) {
                    InspectionSnapshotStatus.REOPENED -> return true
                }
                return false
            }
            InspectionSnapshotStatus.RESOLVED -> {
                when (next) {
                    InspectionSnapshotStatus.REOPENED, InspectionSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            InspectionSnapshotStatus.REOPENED -> {
                when (next) {
                    InspectionSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("inspection snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round(updatedAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): InspectionSnapshot {
        return InspectionSnapshot(
            code = code,
            label = label,
            carrier = carrier,
            updatedAt = updatedAt,
            description = description,
            unit = unit,
            etaMinutes = etaMinutes,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection snapshot in status open. */
fun newInspectionSnapshot(code: String, label: String): InspectionSnapshot {
    return InspectionSnapshot(
        code = code,
        label = label,
        carrier = "",
        updatedAt = 0,
        description = "",
        unit = "",
        etaMinutes = 0,
        notes = "",
        status = InspectionSnapshotStatus.OPEN,
    )
}
