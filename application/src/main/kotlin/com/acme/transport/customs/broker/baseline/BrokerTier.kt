package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker tier is in its life cycle. */
enum class BrokerTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A broker tier as the broker team keeps it. */
data class BrokerTier(
    var code: String,
    var name: String,
    var notes: String,
    var ratio: Double,
    var version: Int,
    var externalRef: String,
    var dock: String,
    var status: BrokerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerTierStatus): Boolean {
        when (status) {
            BrokerTierStatus.PROPOSED -> {
                when (next) {
                    BrokerTierStatus.APPROVED, BrokerTierStatus.REJECTED -> return true
                }
                return false
            }
            BrokerTierStatus.APPROVED -> return next == BrokerTierStatus.WITHDRAWN
            BrokerTierStatus.REJECTED -> return next == BrokerTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BrokerTierStatus) {
        if (!canMoveTo(next)) {
            error("broker tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((version.toDouble() - ratio) * 100) / 100

    fun snapshot(): BrokerTier {
        return BrokerTier(
            code = code,
            name = name,
            notes = notes,
            ratio = ratio,
            version = version,
            externalRef = externalRef,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a broker tier in status proposed. */
fun newBrokerTier(code: String, name: String): BrokerTier {
    return BrokerTier(
        code = code,
        name = name,
        notes = "",
        ratio = 0.0,
        version = 0,
        externalRef = "",
        dock = "",
        status = BrokerTierStatus.PROPOSED,
    )
}
