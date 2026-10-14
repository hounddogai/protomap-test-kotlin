package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.normalize

// The tiered ev charge proposal records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a tiered ev charge proposal is in its life cycle. */
enum class TieredEvChargeProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tiered ev charge proposal, with what the planners need to act on it. */
data class TieredEvChargeProposal(
    var code: String,
    var label: String,
    var ownerId: String,
    var unit: String,
    var channel: String,
    var quantity: Int,
    var status: TieredEvChargeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeProposalStatus): Boolean {
        when (status) {
            TieredEvChargeProposalStatus.DRAFT -> {
                when (next) {
                    TieredEvChargeProposalStatus.PENDING, TieredEvChargeProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredEvChargeProposalStatus.PENDING -> {
                when (next) {
                    TieredEvChargeProposalStatus.ACTIVE, TieredEvChargeProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredEvChargeProposalStatus.ACTIVE -> return next == TieredEvChargeProposalStatus.SUSPENDED || next == TieredEvChargeProposalStatus.CLOSED
            TieredEvChargeProposalStatus.SUSPENDED -> {
                when (next) {
                    TieredEvChargeProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredEvChargeProposalStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered ev charge proposal"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): TieredEvChargeProposal {
        return TieredEvChargeProposal(
            code = code,
            label = label,
            ownerId = ownerId,
            unit = unit,
            channel = channel,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered ev charge proposal in status draft. */
fun newTieredEvChargeProposal(code: String, label: String): TieredEvChargeProposal {
    return TieredEvChargeProposal(
        code = code,
        label = label,
        ownerId = "",
        unit = "",
        channel = "",
        quantity = 0,
        status = TieredEvChargeProposalStatus.DRAFT,
    )
}
