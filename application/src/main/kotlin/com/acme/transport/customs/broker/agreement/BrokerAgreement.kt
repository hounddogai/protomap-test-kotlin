package com.acme.transport.customs.broker.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker agreement records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker agreement is in its life cycle. */
enum class BrokerAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a broker agreement and its current status. */
data class BrokerAgreement(
    var code: String,
    var name: String,
    var odometer: Int,
    var trailerId: String,
    var score: Double,
    var category: String,
    var status: BrokerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerAgreementStatus): Boolean {
        when (status) {
            BrokerAgreementStatus.CREATED -> {
                when (next) {
                    BrokerAgreementStatus.RETIRED -> return true
                }
                return false
            }
            BrokerAgreementStatus.VALIDATED -> {
                when (next) {
                    BrokerAgreementStatus.RETIRED -> return true
                }
                return false
            }
            BrokerAgreementStatus.PUBLISHED -> return next == BrokerAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("broker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round((score + odometer.toDouble()) * 100) / 100

    fun snapshot(): BrokerAgreement {
        return BrokerAgreement(
            code = code,
            name = name,
            odometer = odometer,
            trailerId = trailerId,
            score = score,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a broker agreement in status created. */
fun newBrokerAgreement(code: String, name: String): BrokerAgreement {
    return BrokerAgreement(
        code = code,
        name = name,
        odometer = 0,
        trailerId = "",
        score = 0.0,
        category = "",
        status = BrokerAgreementStatus.CREATED,
    )
}
