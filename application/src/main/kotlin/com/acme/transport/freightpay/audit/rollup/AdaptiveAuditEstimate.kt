package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.normalize

/** Where a adaptive audit estimate is in its life cycle. */
enum class AdaptiveAuditEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive audit estimate as the audit team keeps it. */
data class AdaptiveAuditEstimate(
    var code: String,
    var title: String,
    var carrier: String,
    var sequence: Int,
    var shiftCode: String,
    var status: AdaptiveAuditEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditEstimateStatus): Boolean {
        when (status) {
            AdaptiveAuditEstimateStatus.REQUESTED -> return next == AdaptiveAuditEstimateStatus.REVOKED
            AdaptiveAuditEstimateStatus.GRANTED -> return next == AdaptiveAuditEstimateStatus.REVOKED || next == AdaptiveAuditEstimateStatus.EXPIRED
            AdaptiveAuditEstimateStatus.REVOKED -> return next == AdaptiveAuditEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditEstimateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive audit estimate"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): AdaptiveAuditEstimate {
        return AdaptiveAuditEstimate(
            code = code,
            title = title,
            carrier = carrier,
            sequence = sequence,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive audit estimate in status requested. */
fun newAdaptiveAuditEstimate(code: String, title: String): AdaptiveAuditEstimate {
    return AdaptiveAuditEstimate(
        code = code,
        title = title,
        carrier = "",
        sequence = 0,
        shiftCode = "",
        status = AdaptiveAuditEstimateStatus.REQUESTED,
    )
}
