package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending hub template records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a pending hub template is in its life cycle. */
enum class PendingHubTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending hub template and its current status. */
data class PendingHubTemplate(
    var code: String,
    var title: String,
    var limit: Int,
    var approved: Boolean,
    var routeCode: String,
    var trailerId: String,
    var active: Boolean,
    var graceDays: Int,
    var status: PendingHubTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHubTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHubTemplateStatus): Boolean {
        when (status) {
            PendingHubTemplateStatus.PLANNED -> return next == PendingHubTemplateStatus.SCHEDULED || next == PendingHubTemplateStatus.IN_PROGRESS
            PendingHubTemplateStatus.SCHEDULED -> return next == PendingHubTemplateStatus.IN_PROGRESS || next == PendingHubTemplateStatus.CANCELLED
            PendingHubTemplateStatus.IN_PROGRESS -> return next == PendingHubTemplateStatus.COMPLETED || next == PendingHubTemplateStatus.CANCELLED
            PendingHubTemplateStatus.COMPLETED -> {
                when (next) {
                    PendingHubTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHubTemplateStatus) {
        if (!canMoveTo(next)) {
            error("pending hub template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round((graceDays.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): PendingHubTemplate {
        return PendingHubTemplate(
            code = code,
            title = title,
            limit = limit,
            approved = approved,
            routeCode = routeCode,
            trailerId = trailerId,
            active = active,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending hub template in status planned. */
fun newPendingHubTemplate(code: String, title: String): PendingHubTemplate {
    return PendingHubTemplate(
        code = code,
        title = title,
        limit = 0,
        approved = false,
        routeCode = "",
        trailerId = "",
        active = false,
        graceDays = 0,
        status = PendingHubTemplateStatus.PLANNED,
    )
}
