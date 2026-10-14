package com.acme.transport.customs.broker.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default broker quota records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a default broker quota is in its life cycle. */
enum class DefaultBrokerQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default broker quota and its current status. */
data class DefaultBrokerQuota(
    var code: String,
    var name: String,
    var amount: Double,
    var summary: String,
    var currency: String,
    var carrier: String,
    var channel: String,
    var minimum: Double,
    var status: DefaultBrokerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBrokerQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBrokerQuotaStatus): Boolean {
        when (status) {
            DefaultBrokerQuotaStatus.REQUESTED -> return next == DefaultBrokerQuotaStatus.EXPIRED
            DefaultBrokerQuotaStatus.GRANTED -> {
                when (next) {
                    DefaultBrokerQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultBrokerQuotaStatus.REVOKED -> return next == DefaultBrokerQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultBrokerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("default broker quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default broker quota"} $code"

    fun adjustedTotal(): Double = round((minimum - amount) * 100) / 100

    fun snapshot(): DefaultBrokerQuota {
        return DefaultBrokerQuota(
            code = code,
            name = name,
            amount = amount,
            summary = summary,
            currency = currency,
            carrier = carrier,
            channel = channel,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default broker quota in status requested. */
fun newDefaultBrokerQuota(code: String, name: String): DefaultBrokerQuota {
    return DefaultBrokerQuota(
        code = code,
        name = name,
        amount = 0.0,
        summary = "",
        currency = "",
        carrier = "",
        channel = "",
        minimum = 0.0,
        status = DefaultBrokerQuotaStatus.REQUESTED,
    )
}
