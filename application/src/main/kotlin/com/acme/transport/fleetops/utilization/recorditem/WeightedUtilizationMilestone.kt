package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.normalize

/** Where a weighted utilization milestone is in its life cycle. */
enum class WeightedUtilizationMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted utilization milestone. Its code is unique within a tenant. */
data class WeightedUtilizationMilestone(
    var code: String,
    var title: String,
    var version: Int,
    var contactEmail: String,
    var source: String,
    var reviewer: String,
    var routeCode: String,
    var status: WeightedUtilizationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedUtilizationMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedUtilizationMilestoneStatus): Boolean {
        when (status) {
            WeightedUtilizationMilestoneStatus.DRAFT -> return next == WeightedUtilizationMilestoneStatus.PENDING || next == WeightedUtilizationMilestoneStatus.SUSPENDED
            WeightedUtilizationMilestoneStatus.PENDING -> {
                when (next) {
                    WeightedUtilizationMilestoneStatus.ACTIVE, WeightedUtilizationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            WeightedUtilizationMilestoneStatus.ACTIVE -> {
                when (next) {
                    WeightedUtilizationMilestoneStatus.SUSPENDED, WeightedUtilizationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            WeightedUtilizationMilestoneStatus.SUSPENDED -> {
                when (next) {
                    WeightedUtilizationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedUtilizationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("weighted utilization milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"weighted utilization milestone"} $code"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): WeightedUtilizationMilestone {
        return WeightedUtilizationMilestone(
            code = code,
            title = title,
            version = version,
            contactEmail = contactEmail,
            source = source,
            reviewer = reviewer,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted utilization milestone in status draft. */
fun newWeightedUtilizationMilestone(code: String, title: String): WeightedUtilizationMilestone {
    return WeightedUtilizationMilestone(
        code = code,
        title = title,
        version = 0,
        contactEmail = "",
        source = "",
        reviewer = "",
        routeCode = "",
        status = WeightedUtilizationMilestoneStatus.DRAFT,
    )
}
