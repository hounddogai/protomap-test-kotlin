package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.text.normalize

// The regional trailer milestone records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a regional trailer milestone is in its life cycle. */
enum class RegionalTrailerMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional trailer milestone. Its code is unique within a tenant. */
data class RegionalTrailerMilestone(
    var code: String,
    var title: String,
    var contactEmail: String,
    var archived: Boolean,
    var amount: Double,
    var status: RegionalTrailerMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerMilestoneStatus): Boolean {
        when (status) {
            RegionalTrailerMilestoneStatus.REQUESTED -> {
                when (next) {
                    RegionalTrailerMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalTrailerMilestoneStatus.GRANTED -> return next == RegionalTrailerMilestoneStatus.REVOKED || next == RegionalTrailerMilestoneStatus.EXPIRED
            RegionalTrailerMilestoneStatus.REVOKED -> {
                when (next) {
                    RegionalTrailerMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional trailer milestone"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): RegionalTrailerMilestone {
        return RegionalTrailerMilestone(
            code = code,
            title = title,
            contactEmail = contactEmail,
            archived = archived,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional trailer milestone in status requested. */
fun newRegionalTrailerMilestone(code: String, title: String): RegionalTrailerMilestone {
    return RegionalTrailerMilestone(
        code = code,
        title = title,
        contactEmail = "",
        archived = false,
        amount = 0.0,
        status = RegionalTrailerMilestoneStatus.REQUESTED,
    )
}
