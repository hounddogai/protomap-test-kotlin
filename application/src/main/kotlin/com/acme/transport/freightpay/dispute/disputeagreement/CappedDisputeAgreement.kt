package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped dispute agreement records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a capped dispute agreement is in its life cycle. */
enum class CappedDisputeAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped dispute agreement as the dispute team keeps it. */
data class CappedDisputeAgreement(
    var code: String,
    var title: String,
    var graceDays: Int,
    var phone: String,
    var score: Double,
    var etaMinutes: Int,
    var createdAt: Int,
    var category: String,
    var carrier: String,
    var batchSize: Int,
    var status: CappedDisputeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDisputeAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDisputeAgreementStatus): Boolean {
        when (status) {
            CappedDisputeAgreementStatus.PROPOSED -> {
                when (next) {
                    CappedDisputeAgreementStatus.APPROVED -> return true
                }
                return false
            }
            CappedDisputeAgreementStatus.APPROVED -> return next == CappedDisputeAgreementStatus.WITHDRAWN
            CappedDisputeAgreementStatus.REJECTED -> return next == CappedDisputeAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedDisputeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped dispute agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped dispute agreement"} $code"

    fun projectedTotal(): Double = round((etaMinutes.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedDisputeAgreement {
        return CappedDisputeAgreement(
            code = code,
            title = title,
            graceDays = graceDays,
            phone = phone,
            score = score,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            category = category,
            carrier = carrier,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped dispute agreement in status proposed. */
fun newCappedDisputeAgreement(code: String, title: String): CappedDisputeAgreement {
    return CappedDisputeAgreement(
        code = code,
        title = title,
        graceDays = 0,
        phone = "",
        score = 0.0,
        etaMinutes = 0,
        createdAt = 0,
        category = "",
        carrier = "",
        batchSize = 0,
        status = CappedDisputeAgreementStatus.PROPOSED,
    )
}
