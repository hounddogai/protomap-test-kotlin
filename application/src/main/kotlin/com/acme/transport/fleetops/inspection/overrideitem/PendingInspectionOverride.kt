package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending inspection override records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a pending inspection override is in its life cycle. */
enum class PendingInspectionOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending inspection override as the inspection team keeps it. */
data class PendingInspectionOverride(
    var code: String,
    var title: String,
    var summary: String,
    var revision: Int,
    var limit: Int,
    var name: String,
    var quantity: Int,
    var trailerId: String,
    var sequence: Int,
    var status: PendingInspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionOverrideStatus): Boolean {
        when (status) {
            PendingInspectionOverrideStatus.OPEN -> {
                when (next) {
                    PendingInspectionOverrideStatus.REOPENED, PendingInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingInspectionOverrideStatus.IN_REVIEW -> {
                when (next) {
                    PendingInspectionOverrideStatus.RESOLVED, PendingInspectionOverrideStatus.REOPENED -> return true
                }
                return false
            }
            PendingInspectionOverrideStatus.RESOLVED -> {
                when (next) {
                    PendingInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingInspectionOverrideStatus.REOPENED -> {
                when (next) {
                    PendingInspectionOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection override $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round((quantity.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionOverride {
        return PendingInspectionOverride(
            code = code,
            title = title,
            summary = summary,
            revision = revision,
            limit = limit,
            name = name,
            quantity = quantity,
            trailerId = trailerId,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending inspection override in status open. */
fun newPendingInspectionOverride(code: String, title: String): PendingInspectionOverride {
    return PendingInspectionOverride(
        code = code,
        title = title,
        summary = "",
        revision = 0,
        limit = 0,
        name = "",
        quantity = 0,
        trailerId = "",
        sequence = 0,
        status = PendingInspectionOverrideStatus.OPEN,
    )
}
