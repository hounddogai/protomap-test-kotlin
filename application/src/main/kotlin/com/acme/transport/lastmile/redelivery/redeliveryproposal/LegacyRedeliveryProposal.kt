package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy redelivery proposal records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a legacy redelivery proposal is in its life cycle. */
enum class LegacyRedeliveryProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy redelivery proposal and its current status. */
data class LegacyRedeliveryProposal(
    var code: String,
    var title: String,
    var description: String,
    var segment: String,
    var threshold: Double,
    var ratio: Double,
    var source: String,
    var depot: String,
    var loadKg: Double,
    var capacity: Int,
    var status: LegacyRedeliveryProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRedeliveryProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRedeliveryProposalStatus): Boolean {
        when (status) {
            LegacyRedeliveryProposalStatus.OPEN -> {
                when (next) {
                    LegacyRedeliveryProposalStatus.IN_REVIEW, LegacyRedeliveryProposalStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyRedeliveryProposalStatus.IN_REVIEW -> return next == LegacyRedeliveryProposalStatus.RESOLVED
            LegacyRedeliveryProposalStatus.RESOLVED -> {
                when (next) {
                    LegacyRedeliveryProposalStatus.REOPENED, LegacyRedeliveryProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyRedeliveryProposalStatus.REOPENED -> {
                when (next) {
                    LegacyRedeliveryProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRedeliveryProposalStatus) {
        if (!canMoveTo(next)) {
            error("legacy redelivery proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round(threshold * ratio * 100) / 100

    fun snapshot(): LegacyRedeliveryProposal {
        return LegacyRedeliveryProposal(
            code = code,
            title = title,
            description = description,
            segment = segment,
            threshold = threshold,
            ratio = ratio,
            source = source,
            depot = depot,
            loadKg = loadKg,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy redelivery proposal in status open. */
fun newLegacyRedeliveryProposal(code: String, title: String): LegacyRedeliveryProposal {
    return LegacyRedeliveryProposal(
        code = code,
        title = title,
        description = "",
        segment = "",
        threshold = 0.0,
        ratio = 0.0,
        source = "",
        depot = "",
        loadKg = 0.0,
        capacity = 0,
        status = LegacyRedeliveryProposalStatus.OPEN,
    )
}
