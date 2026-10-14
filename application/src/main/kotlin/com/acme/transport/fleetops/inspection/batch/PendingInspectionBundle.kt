package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection bundle is in its life cycle. */
enum class PendingInspectionBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending inspection bundle, with what the planners need to act on it. */
data class PendingInspectionBundle(
    var code: String,
    var title: String,
    var archived: Boolean,
    var active: Boolean,
    var description: String,
    var distanceKm: Double,
    var capacity: Int,
    var score: Double,
    var version: Int,
    var trailerId: String,
    var status: PendingInspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionBundleStatus): Boolean {
        when (status) {
            PendingInspectionBundleStatus.CREATED -> {
                when (next) {
                    PendingInspectionBundleStatus.RETIRED -> return true
                }
                return false
            }
            PendingInspectionBundleStatus.VALIDATED -> {
                when (next) {
                    PendingInspectionBundleStatus.PUBLISHED, PendingInspectionBundleStatus.RETIRED -> return true
                }
                return false
            }
            PendingInspectionBundleStatus.PUBLISHED -> return next == PendingInspectionBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingInspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection bundle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveValue(): Double = round(capacity.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): PendingInspectionBundle {
        return PendingInspectionBundle(
            code = code,
            title = title,
            archived = archived,
            active = active,
            description = description,
            distanceKm = distanceKm,
            capacity = capacity,
            score = score,
            version = version,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending inspection bundle in status created. */
fun newPendingInspectionBundle(code: String, title: String): PendingInspectionBundle {
    return PendingInspectionBundle(
        code = code,
        title = title,
        archived = false,
        active = false,
        description = "",
        distanceKm = 0.0,
        capacity = 0,
        score = 0.0,
        version = 0,
        trailerId = "",
        status = PendingInspectionBundleStatus.CREATED,
    )
}
