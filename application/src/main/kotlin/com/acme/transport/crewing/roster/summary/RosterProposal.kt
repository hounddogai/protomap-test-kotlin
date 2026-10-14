package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster proposal is in its life cycle. */
enum class RosterProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A roster proposal as the roster team keeps it. */
data class RosterProposal(
    var code: String,
    var title: String,
    var weight: Double,
    var dock: String,
    var windowDays: Int,
    var floorValue: Double,
    var category: String,
    var graceDays: Int,
    var expiresAt: Int,
    var ratio: Double,
    var status: RosterProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterProposalStatus): Boolean {
        when (status) {
            RosterProposalStatus.CREATED -> return next == RosterProposalStatus.VALIDATED || next == RosterProposalStatus.PUBLISHED
            RosterProposalStatus.VALIDATED -> return next == RosterProposalStatus.PUBLISHED || next == RosterProposalStatus.RETIRED
            RosterProposalStatus.PUBLISHED -> {
                when (next) {
                    RosterProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterProposalStatus) {
        if (!canMoveTo(next)) {
            error("roster proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster proposal"} $code"

    fun netExposure(): Double = round((floorValue - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RosterProposal {
        return RosterProposal(
            code = code,
            title = title,
            weight = weight,
            dock = dock,
            windowDays = windowDays,
            floorValue = floorValue,
            category = category,
            graceDays = graceDays,
            expiresAt = expiresAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a roster proposal in status created. */
fun newRosterProposal(code: String, title: String): RosterProposal {
    return RosterProposal(
        code = code,
        title = title,
        weight = 0.0,
        dock = "",
        windowDays = 0,
        floorValue = 0.0,
        category = "",
        graceDays = 0,
        expiresAt = 0,
        ratio = 0.0,
        status = RosterProposalStatus.CREATED,
    )
}
