package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery agreement is in its life cycle. */
enum class ProofOfDeliveryAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A proof of delivery agreement. Its code is unique within a tenant. */
data class ProofOfDeliveryAgreement(
    var code: String,
    var name: String,
    var revision: Int,
    var expiresAt: Int,
    var windowDays: Int,
    var status: ProofOfDeliveryAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryAgreementStatus): Boolean {
        when (status) {
            ProofOfDeliveryAgreementStatus.OPEN -> return next == ProofOfDeliveryAgreementStatus.ARCHIVED
            ProofOfDeliveryAgreementStatus.IN_REVIEW -> {
                when (next) {
                    ProofOfDeliveryAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ProofOfDeliveryAgreementStatus.RESOLVED -> {
                when (next) {
                    ProofOfDeliveryAgreementStatus.REOPENED, ProofOfDeliveryAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ProofOfDeliveryAgreementStatus.REOPENED -> return next == ProofOfDeliveryAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryAgreementStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"proof of delivery agreement"} $code"

    fun netTotal(): Double = round(windowDays.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryAgreement {
        return ProofOfDeliveryAgreement(
            code = code,
            name = name,
            revision = revision,
            expiresAt = expiresAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery agreement in status open. */
fun newProofOfDeliveryAgreement(code: String, name: String): ProofOfDeliveryAgreement {
    return ProofOfDeliveryAgreement(
        code = code,
        name = name,
        revision = 0,
        expiresAt = 0,
        windowDays = 0,
        status = ProofOfDeliveryAgreementStatus.OPEN,
    )
}
