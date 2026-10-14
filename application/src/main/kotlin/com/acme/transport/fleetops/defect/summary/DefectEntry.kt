package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect entry is in its life cycle. */
enum class DefectEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One defect entry, with what the planners need to act on it. */
data class DefectEntry(
    var code: String,
    var title: String,
    var unit: String,
    var category: String,
    var notes: String,
    var region: String,
    var distanceKm: Double,
    var expiresAt: Int,
    var reviewer: String,
    var depot: String,
    var active: Boolean,
    var status: DefectEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectEntryStatus): Boolean {
        when (status) {
            DefectEntryStatus.OPEN -> {
                when (next) {
                    DefectEntryStatus.IN_REVIEW -> return true
                }
                return false
            }
            DefectEntryStatus.IN_REVIEW -> return next == DefectEntryStatus.ARCHIVED
            DefectEntryStatus.RESOLVED -> return next == DefectEntryStatus.REOPENED
            DefectEntryStatus.REOPENED -> {
                when (next) {
                    DefectEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectEntryStatus) {
        if (!canMoveTo(next)) {
            error("defect entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((expiresAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): DefectEntry {
        return DefectEntry(
            code = code,
            title = title,
            unit = unit,
            category = category,
            notes = notes,
            region = region,
            distanceKm = distanceKm,
            expiresAt = expiresAt,
            reviewer = reviewer,
            depot = depot,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a defect entry in status open. */
fun newDefectEntry(code: String, title: String): DefectEntry {
    return DefectEntry(
        code = code,
        title = title,
        unit = "",
        category = "",
        notes = "",
        region = "",
        distanceKm = 0.0,
        expiresAt = 0,
        reviewer = "",
        depot = "",
        active = false,
        status = DefectEntryStatus.OPEN,
    )
}
