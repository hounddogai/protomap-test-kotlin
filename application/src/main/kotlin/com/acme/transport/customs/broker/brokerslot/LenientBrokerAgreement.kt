package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient broker agreement is in its life cycle. */
enum class LenientBrokerAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient broker agreement and its current status. */
data class LenientBrokerAgreement(
    var code: String,
    var title: String,
    var loadKg: Double,
    var pallets: Int,
    var expiresAt: Int,
    var phone: String,
    var revision: Int,
    var budget: Double,
    var discount: Double,
    var status: LenientBrokerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBrokerAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBrokerAgreementStatus): Boolean {
        when (status) {
            LenientBrokerAgreementStatus.REQUESTED -> {
                when (next) {
                    LenientBrokerAgreementStatus.GRANTED -> return true
                }
                return false
            }
            LenientBrokerAgreementStatus.GRANTED -> return next == LenientBrokerAgreementStatus.REVOKED
            LenientBrokerAgreementStatus.REVOKED -> return next == LenientBrokerAgreementStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientBrokerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lenient broker agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(discount * expiresAt.toDouble() * 100) / 100

    fun snapshot(): LenientBrokerAgreement {
        return LenientBrokerAgreement(
            code = code,
            title = title,
            loadKg = loadKg,
            pallets = pallets,
            expiresAt = expiresAt,
            phone = phone,
            revision = revision,
            budget = budget,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient broker agreement in status requested. */
fun newLenientBrokerAgreement(code: String, title: String): LenientBrokerAgreement {
    return LenientBrokerAgreement(
        code = code,
        title = title,
        loadKg = 0.0,
        pallets = 0,
        expiresAt = 0,
        phone = "",
        revision = 0,
        budget = 0.0,
        discount = 0.0,
        status = LenientBrokerAgreementStatus.REQUESTED,
    )
}
