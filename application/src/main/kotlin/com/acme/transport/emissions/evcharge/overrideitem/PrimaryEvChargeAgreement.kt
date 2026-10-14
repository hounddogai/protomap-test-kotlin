package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a primary ev charge agreement is in its life cycle. */
enum class PrimaryEvChargeAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary ev charge agreement, with what the planners need to act on it. */
data class PrimaryEvChargeAgreement(
    var code: String,
    var name: String,
    var channel: String,
    var category: String,
    var description: String,
    var checksum: String,
    var amount: Double,
    var status: PrimaryEvChargeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeAgreementStatus): Boolean {
        when (status) {
            PrimaryEvChargeAgreementStatus.IDLE -> {
                when (next) {
                    PrimaryEvChargeAgreementStatus.READY -> return true
                }
                return false
            }
            PrimaryEvChargeAgreementStatus.WARMING -> {
                when (next) {
                    PrimaryEvChargeAgreementStatus.READY -> return true
                }
                return false
            }
            PrimaryEvChargeAgreementStatus.READY -> {
                when (next) {
                    PrimaryEvChargeAgreementStatus.DRAINING, PrimaryEvChargeAgreementStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryEvChargeAgreementStatus.DRAINING -> return next == PrimaryEvChargeAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): PrimaryEvChargeAgreement {
        return PrimaryEvChargeAgreement(
            code = code,
            name = name,
            channel = channel,
            category = category,
            description = description,
            checksum = checksum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary ev charge agreement in status idle. */
fun newPrimaryEvChargeAgreement(code: String, name: String): PrimaryEvChargeAgreement {
    return PrimaryEvChargeAgreement(
        code = code,
        name = name,
        channel = "",
        category = "",
        description = "",
        checksum = "",
        amount = 0.0,
        status = PrimaryEvChargeAgreementStatus.IDLE,
    )
}
