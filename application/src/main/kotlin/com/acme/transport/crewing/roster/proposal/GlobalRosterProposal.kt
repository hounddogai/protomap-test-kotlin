package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global roster proposal records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a global roster proposal is in its life cycle. */
enum class GlobalRosterProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global roster proposal and its current status. */
data class GlobalRosterProposal(
    var code: String,
    var name: String,
    var currency: String,
    var revision: Int,
    var sequence: Int,
    var priority: Int,
    var expiresAt: Int,
    var startsAt: Int,
    var tags: MutableList<String>,
    var shiftCode: String,
    var notes: String,
    var status: GlobalRosterProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRosterProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRosterProposalStatus): Boolean {
        when (status) {
            GlobalRosterProposalStatus.OPEN -> {
                when (next) {
                    GlobalRosterProposalStatus.RESOLVED, GlobalRosterProposalStatus.REOPENED -> return true
                }
                return false
            }
            GlobalRosterProposalStatus.IN_REVIEW -> return next == GlobalRosterProposalStatus.ARCHIVED
            GlobalRosterProposalStatus.RESOLVED -> return next == GlobalRosterProposalStatus.REOPENED || next == GlobalRosterProposalStatus.ARCHIVED
            GlobalRosterProposalStatus.REOPENED -> return next == GlobalRosterProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalRosterProposalStatus) {
        if (!canMoveTo(next)) {
            error("global roster proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((startsAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): GlobalRosterProposal {
        return GlobalRosterProposal(
            code = code,
            name = name,
            currency = currency,
            revision = revision,
            sequence = sequence,
            priority = priority,
            expiresAt = expiresAt,
            startsAt = startsAt,
            tags = tags,
            shiftCode = shiftCode,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global roster proposal in status open. */
fun newGlobalRosterProposal(code: String, name: String): GlobalRosterProposal {
    return GlobalRosterProposal(
        code = code,
        name = name,
        currency = "",
        revision = 0,
        sequence = 0,
        priority = 0,
        expiresAt = 0,
        startsAt = 0,
        tags = mutableListOf<String>(),
        shiftCode = "",
        notes = "",
        status = GlobalRosterProposalStatus.OPEN,
    )
}
