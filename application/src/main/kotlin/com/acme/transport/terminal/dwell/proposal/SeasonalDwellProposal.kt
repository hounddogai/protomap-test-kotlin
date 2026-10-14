package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal dwell proposal records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a seasonal dwell proposal is in its life cycle. */
enum class SeasonalDwellProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a seasonal dwell proposal and its current status. */
data class SeasonalDwellProposal(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var status: SeasonalDwellProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDwellProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDwellProposalStatus): Boolean {
        when (status) {
            SeasonalDwellProposalStatus.PROPOSED -> return next == SeasonalDwellProposalStatus.REJECTED || next == SeasonalDwellProposalStatus.WITHDRAWN
            SeasonalDwellProposalStatus.APPROVED -> return next == SeasonalDwellProposalStatus.REJECTED || next == SeasonalDwellProposalStatus.WITHDRAWN
            SeasonalDwellProposalStatus.REJECTED -> {
                when (next) {
                    SeasonalDwellProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDwellProposalStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dwell proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((threshold + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDwellProposal {
        return SeasonalDwellProposal(
            code = code,
            title = title,
            expiresAt = expiresAt,
            approved = approved,
            attributes = attributes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal dwell proposal in status proposed. */
fun newSeasonalDwellProposal(code: String, title: String): SeasonalDwellProposal {
    return SeasonalDwellProposal(
        code = code,
        title = title,
        expiresAt = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        status = SeasonalDwellProposalStatus.PROPOSED,
    )
}
