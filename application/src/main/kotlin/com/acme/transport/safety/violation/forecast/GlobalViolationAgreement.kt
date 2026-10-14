package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.text.normalize

/** Where a global violation agreement is in its life cycle. */
enum class GlobalViolationAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global violation agreement and its current status. */
data class GlobalViolationAgreement(
    var code: String,
    var title: String,
    var channel: String,
    var timezone: String,
    var lane: String,
    var currency: String,
    var quantity: Int,
    var status: GlobalViolationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationAgreementStatus): Boolean {
        when (status) {
            GlobalViolationAgreementStatus.PROPOSED -> {
                when (next) {
                    GlobalViolationAgreementStatus.REJECTED, GlobalViolationAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalViolationAgreementStatus.APPROVED -> return next == GlobalViolationAgreementStatus.REJECTED || next == GlobalViolationAgreementStatus.WITHDRAWN
            GlobalViolationAgreementStatus.REJECTED -> return next == GlobalViolationAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalViolationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("global violation agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global violation agreement"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): GlobalViolationAgreement {
        return GlobalViolationAgreement(
            code = code,
            title = title,
            channel = channel,
            timezone = timezone,
            lane = lane,
            currency = currency,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global violation agreement in status proposed. */
fun newGlobalViolationAgreement(code: String, title: String): GlobalViolationAgreement {
    return GlobalViolationAgreement(
        code = code,
        title = title,
        channel = "",
        timezone = "",
        lane = "",
        currency = "",
        quantity = 0,
        status = GlobalViolationAgreementStatus.PROPOSED,
    )
}
