package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.normalize

// The broker override records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker override is in its life cycle. */
enum class BrokerOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a broker override and its current status. */
data class BrokerOverride(
    var code: String,
    var label: String,
    var currency: String,
    var depot: String,
    var segment: String,
    var unit: String,
    var tags: MutableList<String>,
    var source: String,
    var quantity: Int,
    var status: BrokerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerOverrideStatus): Boolean {
        when (status) {
            BrokerOverrideStatus.DRAFT -> return next == BrokerOverrideStatus.SUSPENDED
            BrokerOverrideStatus.PENDING -> {
                when (next) {
                    BrokerOverrideStatus.ACTIVE, BrokerOverrideStatus.CLOSED -> return true
                }
                return false
            }
            BrokerOverrideStatus.ACTIVE -> return next == BrokerOverrideStatus.SUSPENDED
            BrokerOverrideStatus.SUSPENDED -> return next == BrokerOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BrokerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("broker override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"broker override"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): BrokerOverride {
        return BrokerOverride(
            code = code,
            label = label,
            currency = currency,
            depot = depot,
            segment = segment,
            unit = unit,
            tags = tags,
            source = source,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a broker override in status draft. */
fun newBrokerOverride(code: String, label: String): BrokerOverride {
    return BrokerOverride(
        code = code,
        label = label,
        currency = "",
        depot = "",
        segment = "",
        unit = "",
        tags = mutableListOf<String>(),
        source = "",
        quantity = 0,
        status = BrokerOverrideStatus.DRAFT,
    )
}
