package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cutoff milestone is in its life cycle. */
enum class PendingCutoffMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending cutoff milestone and its current status. */
data class PendingCutoffMilestone(
    var code: String,
    var label: String,
    var odometer: Int,
    var source: String,
    var threshold: Double,
    var reviewer: String,
    var version: Int,
    var distanceKm: Double,
    var dock: String,
    var capacity: Int,
    var status: PendingCutoffMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCutoffMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCutoffMilestoneStatus): Boolean {
        when (status) {
            PendingCutoffMilestoneStatus.CREATED -> {
                when (next) {
                    PendingCutoffMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            PendingCutoffMilestoneStatus.VALIDATED -> return next == PendingCutoffMilestoneStatus.PUBLISHED
            PendingCutoffMilestoneStatus.PUBLISHED -> {
                when (next) {
                    PendingCutoffMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCutoffMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending cutoff milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round(version.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): PendingCutoffMilestone {
        return PendingCutoffMilestone(
            code = code,
            label = label,
            odometer = odometer,
            source = source,
            threshold = threshold,
            reviewer = reviewer,
            version = version,
            distanceKm = distanceKm,
            dock = dock,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending cutoff milestone in status created. */
fun newPendingCutoffMilestone(code: String, label: String): PendingCutoffMilestone {
    return PendingCutoffMilestone(
        code = code,
        label = label,
        odometer = 0,
        source = "",
        threshold = 0.0,
        reviewer = "",
        version = 0,
        distanceKm = 0.0,
        dock = "",
        capacity = 0,
        status = PendingCutoffMilestoneStatus.CREATED,
    )
}
