package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery proposal is in its life cycle. */
enum class ProofOfDeliveryProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A proof of delivery proposal as the proof of delivery team keeps it. */
data class ProofOfDeliveryProposal(
    var code: String,
    var title: String,
    var startsAt: Int,
    var amount: Double,
    var limit: Int,
    var durationMinutes: Int,
    var loadKg: Double,
    var status: ProofOfDeliveryProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryProposalStatus): Boolean {
        when (status) {
            ProofOfDeliveryProposalStatus.CREATED -> return next == ProofOfDeliveryProposalStatus.VALIDATED || next == ProofOfDeliveryProposalStatus.PUBLISHED
            ProofOfDeliveryProposalStatus.VALIDATED -> return next == ProofOfDeliveryProposalStatus.RETIRED
            ProofOfDeliveryProposalStatus.PUBLISHED -> return next == ProofOfDeliveryProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryProposalStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery proposal $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"proof of delivery proposal"} $code"

    fun netTotal(): Double = round((amount + limit.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryProposal {
        return ProofOfDeliveryProposal(
            code = code,
            title = title,
            startsAt = startsAt,
            amount = amount,
            limit = limit,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery proposal in status created. */
fun newProofOfDeliveryProposal(code: String, title: String): ProofOfDeliveryProposal {
    return ProofOfDeliveryProposal(
        code = code,
        title = title,
        startsAt = 0,
        amount = 0.0,
        limit = 0,
        durationMinutes = 0,
        loadKg = 0.0,
        status = ProofOfDeliveryProposalStatus.CREATED,
    )
}
