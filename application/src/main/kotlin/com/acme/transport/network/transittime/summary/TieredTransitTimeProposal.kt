package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.normalize

/** Where a tiered transit time proposal is in its life cycle. */
enum class TieredTransitTimeProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered transit time proposal. Its code is unique within a tenant. */
data class TieredTransitTimeProposal(
    var code: String,
    var label: String,
    var depot: String,
    var source: String,
    var pinned: Boolean,
    var sequence: Int,
    var country: String,
    var description: String,
    var status: TieredTransitTimeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeProposalStatus): Boolean {
        when (status) {
            TieredTransitTimeProposalStatus.IDLE -> {
                when (next) {
                    TieredTransitTimeProposalStatus.READY -> return true
                }
                return false
            }
            TieredTransitTimeProposalStatus.WARMING -> {
                when (next) {
                    TieredTransitTimeProposalStatus.READY -> return true
                }
                return false
            }
            TieredTransitTimeProposalStatus.READY -> {
                when (next) {
                    TieredTransitTimeProposalStatus.DRAINING -> return true
                }
                return false
            }
            TieredTransitTimeProposalStatus.DRAINING -> return next == TieredTransitTimeProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeProposalStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): TieredTransitTimeProposal {
        return TieredTransitTimeProposal(
            code = code,
            label = label,
            depot = depot,
            source = source,
            pinned = pinned,
            sequence = sequence,
            country = country,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered transit time proposal in status idle. */
fun newTieredTransitTimeProposal(code: String, label: String): TieredTransitTimeProposal {
    return TieredTransitTimeProposal(
        code = code,
        label = label,
        depot = "",
        source = "",
        pinned = false,
        sequence = 0,
        country = "",
        description = "",
        status = TieredTransitTimeProposalStatus.IDLE,
    )
}
