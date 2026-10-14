package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire proposal records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire proposal is in its life cycle. */
enum class TireProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tire proposal. Its code is unique within a tenant. */
data class TireProposal(
    var code: String,
    var label: String,
    var driverId: String,
    var attempts: Int,
    var cost: Double,
    var quantity: Int,
    var title: String,
    var expiresAt: Int,
    var status: TireProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireProposalStatus): Boolean {
        when (status) {
            TireProposalStatus.REQUESTED -> return next == TireProposalStatus.GRANTED || next == TireProposalStatus.EXPIRED
            TireProposalStatus.GRANTED -> {
                when (next) {
                    TireProposalStatus.EXPIRED -> return true
                }
                return false
            }
            TireProposalStatus.REVOKED -> {
                when (next) {
                    TireProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireProposalStatus) {
        if (!canMoveTo(next)) {
            error("tire proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round(attempts.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): TireProposal {
        return TireProposal(
            code = code,
            label = label,
            driverId = driverId,
            attempts = attempts,
            cost = cost,
            quantity = quantity,
            title = title,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire proposal in status requested. */
fun newTireProposal(code: String, label: String): TireProposal {
    return TireProposal(
        code = code,
        label = label,
        driverId = "",
        attempts = 0,
        cost = 0.0,
        quantity = 0,
        title = "",
        expiresAt = 0,
        status = TireProposalStatus.REQUESTED,
    )
}
