package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict cutoff target records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a strict cutoff target is in its life cycle. */
enum class StrictCutoffTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict cutoff target. Its code is unique within a tenant. */
data class StrictCutoffTarget(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var distanceKm: Double,
    var batchSize: Int,
    var status: StrictCutoffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCutoffTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCutoffTargetStatus): Boolean {
        when (status) {
            StrictCutoffTargetStatus.OPEN -> return next == StrictCutoffTargetStatus.REOPENED || next == StrictCutoffTargetStatus.ARCHIVED
            StrictCutoffTargetStatus.IN_REVIEW -> {
                when (next) {
                    StrictCutoffTargetStatus.RESOLVED, StrictCutoffTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictCutoffTargetStatus.RESOLVED -> return next == StrictCutoffTargetStatus.REOPENED || next == StrictCutoffTargetStatus.ARCHIVED
            StrictCutoffTargetStatus.REOPENED -> {
                when (next) {
                    StrictCutoffTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCutoffTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict cutoff target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((distanceKm - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): StrictCutoffTarget {
        return StrictCutoffTarget(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict cutoff target in status open. */
fun newStrictCutoffTarget(code: String, name: String): StrictCutoffTarget {
    return StrictCutoffTarget(
        code = code,
        name = name,
        etaMinutes = 0,
        distanceKm = 0.0,
        batchSize = 0,
        status = StrictCutoffTargetStatus.OPEN,
    )
}
