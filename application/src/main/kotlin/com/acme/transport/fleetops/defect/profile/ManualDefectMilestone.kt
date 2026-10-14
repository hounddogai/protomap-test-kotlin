package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual defect milestone is in its life cycle. */
enum class ManualDefectMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual defect milestone and its current status. */
data class ManualDefectMilestone(
    var code: String,
    var name: String,
    var title: String,
    var retries: Int,
    var country: String,
    var description: String,
    var endsAt: Int,
    var graceDays: Int,
    var reviewer: String,
    var carrier: String,
    var ceiling: Double,
    var status: ManualDefectMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDefectMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDefectMilestoneStatus): Boolean {
        when (status) {
            ManualDefectMilestoneStatus.IDLE -> {
                when (next) {
                    ManualDefectMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            ManualDefectMilestoneStatus.WARMING -> return next == ManualDefectMilestoneStatus.READY || next == ManualDefectMilestoneStatus.DRAINING
            ManualDefectMilestoneStatus.READY -> return next == ManualDefectMilestoneStatus.DRAINING || next == ManualDefectMilestoneStatus.STOPPED
            ManualDefectMilestoneStatus.DRAINING -> {
                when (next) {
                    ManualDefectMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDefectMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("manual defect milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((endsAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): ManualDefectMilestone {
        return ManualDefectMilestone(
            code = code,
            name = name,
            title = title,
            retries = retries,
            country = country,
            description = description,
            endsAt = endsAt,
            graceDays = graceDays,
            reviewer = reviewer,
            carrier = carrier,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual defect milestone in status idle. */
fun newManualDefectMilestone(code: String, name: String): ManualDefectMilestone {
    return ManualDefectMilestone(
        code = code,
        name = name,
        title = "",
        retries = 0,
        country = "",
        description = "",
        endsAt = 0,
        graceDays = 0,
        reviewer = "",
        carrier = "",
        ceiling = 0.0,
        status = ManualDefectMilestoneStatus.IDLE,
    )
}
