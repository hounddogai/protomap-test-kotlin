package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.text.normalize

// The pending tariff agreement records of customs's tariff work, and the rules, stores, and calculations
// around them.

/** Where a pending tariff agreement is in its life cycle. */
enum class PendingTariffAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending tariff agreement. Its code is unique within a tenant. */
data class PendingTariffAgreement(
    var code: String,
    var title: String,
    var currency: String,
    var segment: String,
    var stopCount: Int,
    var status: PendingTariffAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTariffAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTariffAgreementStatus): Boolean {
        when (status) {
            PendingTariffAgreementStatus.PROPOSED -> return next == PendingTariffAgreementStatus.APPROVED
            PendingTariffAgreementStatus.APPROVED -> {
                when (next) {
                    PendingTariffAgreementStatus.REJECTED, PendingTariffAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTariffAgreementStatus.REJECTED -> return next == PendingTariffAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingTariffAgreementStatus) {
        if (!canMoveTo(next)) {
            error("pending tariff agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): PendingTariffAgreement {
        return PendingTariffAgreement(
            code = code,
            title = title,
            currency = currency,
            segment = segment,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending tariff agreement in status proposed. */
fun newPendingTariffAgreement(code: String, title: String): PendingTariffAgreement {
    return PendingTariffAgreement(
        code = code,
        title = title,
        currency = "",
        segment = "",
        stopCount = 0,
        status = PendingTariffAgreementStatus.PROPOSED,
    )
}
