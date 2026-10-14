package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize

/** Where a primary cross dock milestone is in its life cycle. */
enum class PrimaryCrossDockMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary cross dock milestone. Its code is unique within a tenant. */
data class PrimaryCrossDockMilestone(
    var code: String,
    var name: String,
    var reviewer: String,
    var externalRef: String,
    var currency: String,
    var routeCode: String,
    var summary: String,
    var version: Int,
    var status: PrimaryCrossDockMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCrossDockMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCrossDockMilestoneStatus): Boolean {
        when (status) {
            PrimaryCrossDockMilestoneStatus.PROPOSED -> {
                when (next) {
                    PrimaryCrossDockMilestoneStatus.APPROVED, PrimaryCrossDockMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryCrossDockMilestoneStatus.APPROVED -> return next == PrimaryCrossDockMilestoneStatus.REJECTED || next == PrimaryCrossDockMilestoneStatus.WITHDRAWN
            PrimaryCrossDockMilestoneStatus.REJECTED -> {
                when (next) {
                    PrimaryCrossDockMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCrossDockMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("primary cross dock milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): PrimaryCrossDockMilestone {
        return PrimaryCrossDockMilestone(
            code = code,
            name = name,
            reviewer = reviewer,
            externalRef = externalRef,
            currency = currency,
            routeCode = routeCode,
            summary = summary,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary cross dock milestone in status proposed. */
fun newPrimaryCrossDockMilestone(code: String, name: String): PrimaryCrossDockMilestone {
    return PrimaryCrossDockMilestone(
        code = code,
        name = name,
        reviewer = "",
        externalRef = "",
        currency = "",
        routeCode = "",
        summary = "",
        version = 0,
        status = PrimaryCrossDockMilestoneStatus.PROPOSED,
    )
}
