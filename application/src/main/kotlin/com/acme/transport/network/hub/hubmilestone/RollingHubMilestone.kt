package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling hub milestone records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a rolling hub milestone is in its life cycle. */
enum class RollingHubMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling hub milestone, with what the planners need to act on it. */
data class RollingHubMilestone(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var createdAt: Int,
    var contactEmail: String,
    var loadKg: Double,
    var routeCode: String,
    var maximum: Double,
    var trailerId: String,
    var status: RollingHubMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingHubMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingHubMilestoneStatus): Boolean {
        when (status) {
            RollingHubMilestoneStatus.OPEN -> {
                when (next) {
                    RollingHubMilestoneStatus.IN_REVIEW, RollingHubMilestoneStatus.RESOLVED -> return true
                }
                return false
            }
            RollingHubMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    RollingHubMilestoneStatus.RESOLVED, RollingHubMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            RollingHubMilestoneStatus.RESOLVED -> return next == RollingHubMilestoneStatus.REOPENED || next == RollingHubMilestoneStatus.ARCHIVED
            RollingHubMilestoneStatus.REOPENED -> return next == RollingHubMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingHubMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling hub milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round(createdAt.toDouble() * maximum * 100) / 100

    fun snapshot(): RollingHubMilestone {
        return RollingHubMilestone(
            code = code,
            title = title,
            expiresAt = expiresAt,
            createdAt = createdAt,
            contactEmail = contactEmail,
            loadKg = loadKg,
            routeCode = routeCode,
            maximum = maximum,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling hub milestone in status open. */
fun newRollingHubMilestone(code: String, title: String): RollingHubMilestone {
    return RollingHubMilestone(
        code = code,
        title = title,
        expiresAt = 0,
        createdAt = 0,
        contactEmail = "",
        loadKg = 0.0,
        routeCode = "",
        maximum = 0.0,
        trailerId = "",
        status = RollingHubMilestoneStatus.OPEN,
    )
}
