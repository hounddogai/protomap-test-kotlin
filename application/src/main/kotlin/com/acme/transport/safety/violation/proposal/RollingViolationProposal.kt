package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling violation proposal records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a rolling violation proposal is in its life cycle. */
enum class RollingViolationProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling violation proposal, with what the planners need to act on it. */
data class RollingViolationProposal(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var etaMinutes: Int,
    var distanceKm: Double,
    var amount: Double,
    var rate: Double,
    var contactEmail: String,
    var category: String,
    var status: RollingViolationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationProposalStatus): Boolean {
        when (status) {
            RollingViolationProposalStatus.CREATED -> {
                when (next) {
                    RollingViolationProposalStatus.VALIDATED -> return true
                }
                return false
            }
            RollingViolationProposalStatus.VALIDATED -> {
                when (next) {
                    RollingViolationProposalStatus.PUBLISHED, RollingViolationProposalStatus.RETIRED -> return true
                }
                return false
            }
            RollingViolationProposalStatus.PUBLISHED -> return next == RollingViolationProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingViolationProposalStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling violation proposal"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(distanceKm * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): RollingViolationProposal {
        return RollingViolationProposal(
            code = code,
            label = label,
            tags = tags,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            amount = amount,
            rate = rate,
            contactEmail = contactEmail,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling violation proposal in status created. */
fun newRollingViolationProposal(code: String, label: String): RollingViolationProposal {
    return RollingViolationProposal(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        etaMinutes = 0,
        distanceKm = 0.0,
        amount = 0.0,
        rate = 0.0,
        contactEmail = "",
        category = "",
        status = RollingViolationProposalStatus.CREATED,
    )
}
