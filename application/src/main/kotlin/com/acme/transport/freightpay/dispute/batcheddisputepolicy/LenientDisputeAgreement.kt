package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dispute agreement is in its life cycle. */
enum class LenientDisputeAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient dispute agreement and its current status. */
data class LenientDisputeAgreement(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var sequence: Int,
    var margin: Double,
    var status: LenientDisputeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputeAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputeAgreementStatus): Boolean {
        when (status) {
            LenientDisputeAgreementStatus.CREATED -> return next == LenientDisputeAgreementStatus.PUBLISHED
            LenientDisputeAgreementStatus.VALIDATED -> {
                when (next) {
                    LenientDisputeAgreementStatus.PUBLISHED, LenientDisputeAgreementStatus.RETIRED -> return true
                }
                return false
            }
            LenientDisputeAgreementStatus.PUBLISHED -> {
                when (next) {
                    LenientDisputeAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDisputeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient dispute agreement"} $code"

    fun projectedExposure(): Double = round(sequence.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): LenientDisputeAgreement {
        return LenientDisputeAgreement(
            code = code,
            name = name,
            updatedAt = updatedAt,
            sequence = sequence,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient dispute agreement in status created. */
fun newLenientDisputeAgreement(code: String, name: String): LenientDisputeAgreement {
    return LenientDisputeAgreement(
        code = code,
        name = name,
        updatedAt = 0,
        sequence = 0,
        margin = 0.0,
        status = LenientDisputeAgreementStatus.CREATED,
    )
}
