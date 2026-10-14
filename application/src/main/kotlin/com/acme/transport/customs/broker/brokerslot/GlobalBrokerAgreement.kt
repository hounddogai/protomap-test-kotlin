package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global broker agreement is in its life cycle. */
enum class GlobalBrokerAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a global broker agreement and its current status. */
data class GlobalBrokerAgreement(
    var code: String,
    var label: String,
    var quantity: Int,
    var source: String,
    var locale: String,
    var durationMinutes: Int,
    var status: GlobalBrokerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBrokerAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBrokerAgreementStatus): Boolean {
        when (status) {
            GlobalBrokerAgreementStatus.IDLE -> {
                when (next) {
                    GlobalBrokerAgreementStatus.READY, GlobalBrokerAgreementStatus.DRAINING -> return true
                }
                return false
            }
            GlobalBrokerAgreementStatus.WARMING -> {
                when (next) {
                    GlobalBrokerAgreementStatus.STOPPED -> return true
                }
                return false
            }
            GlobalBrokerAgreementStatus.READY -> return next == GlobalBrokerAgreementStatus.DRAINING || next == GlobalBrokerAgreementStatus.STOPPED
            GlobalBrokerAgreementStatus.DRAINING -> return next == GlobalBrokerAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalBrokerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("global broker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round(quantity.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): GlobalBrokerAgreement {
        return GlobalBrokerAgreement(
            code = code,
            label = label,
            quantity = quantity,
            source = source,
            locale = locale,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global broker agreement in status idle. */
fun newGlobalBrokerAgreement(code: String, label: String): GlobalBrokerAgreement {
    return GlobalBrokerAgreement(
        code = code,
        label = label,
        quantity = 0,
        source = "",
        locale = "",
        durationMinutes = 0,
        status = GlobalBrokerAgreementStatus.IDLE,
    )
}
