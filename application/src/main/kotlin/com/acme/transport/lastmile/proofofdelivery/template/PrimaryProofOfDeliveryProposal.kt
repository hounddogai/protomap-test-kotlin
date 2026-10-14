package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** Where a primary proof of delivery proposal is in its life cycle. */
enum class PrimaryProofOfDeliveryProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary proof of delivery proposal as the proof of delivery team keeps it. */
data class PrimaryProofOfDeliveryProposal(
    var code: String,
    var title: String,
    var attempts: Int,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var status: PrimaryProofOfDeliveryProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryProofOfDeliveryProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryProofOfDeliveryProposalStatus): Boolean {
        when (status) {
            PrimaryProofOfDeliveryProposalStatus.PLANNED -> return next == PrimaryProofOfDeliveryProposalStatus.COMPLETED
            PrimaryProofOfDeliveryProposalStatus.SCHEDULED -> {
                when (next) {
                    PrimaryProofOfDeliveryProposalStatus.IN_PROGRESS, PrimaryProofOfDeliveryProposalStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryProofOfDeliveryProposalStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryProofOfDeliveryProposalStatus.COMPLETED, PrimaryProofOfDeliveryProposalStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryProofOfDeliveryProposalStatus.COMPLETED -> return next == PrimaryProofOfDeliveryProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryProofOfDeliveryProposalStatus) {
        if (!canMoveTo(next)) {
            error("primary proof of delivery proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): PrimaryProofOfDeliveryProposal {
        return PrimaryProofOfDeliveryProposal(
            code = code,
            title = title,
            attempts = attempts,
            attributes = attributes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary proof of delivery proposal in status planned. */
fun newPrimaryProofOfDeliveryProposal(code: String, title: String): PrimaryProofOfDeliveryProposal {
    return PrimaryProofOfDeliveryProposal(
        code = code,
        title = title,
        attempts = 0,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        status = PrimaryProofOfDeliveryProposalStatus.PLANNED,
    )
}
