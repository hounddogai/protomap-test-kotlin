package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker claim is in its life cycle. */
enum class BrokerClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A broker claim as the broker team keeps it. */
data class BrokerClaim(
    var code: String,
    var name: String,
    var source: String,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var lane: String,
    var loadKg: Double,
    var batchSize: Int,
    var pinned: Boolean,
    var amount: Double,
    var status: BrokerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerClaimStatus): Boolean {
        when (status) {
            BrokerClaimStatus.REQUESTED -> {
                when (next) {
                    BrokerClaimStatus.REVOKED -> return true
                }
                return false
            }
            BrokerClaimStatus.GRANTED -> {
                when (next) {
                    BrokerClaimStatus.EXPIRED -> return true
                }
                return false
            }
            BrokerClaimStatus.REVOKED -> {
                when (next) {
                    BrokerClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerClaimStatus) {
        if (!canMoveTo(next)) {
            error("broker claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((batchSize.toDouble() + amount) * 100) / 100

    fun snapshot(): BrokerClaim {
        return BrokerClaim(
            code = code,
            name = name,
            source = source,
            attributes = attributes,
            summary = summary,
            lane = lane,
            loadKg = loadKg,
            batchSize = batchSize,
            pinned = pinned,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a broker claim in status requested. */
fun newBrokerClaim(code: String, name: String): BrokerClaim {
    return BrokerClaim(
        code = code,
        name = name,
        source = "",
        attributes = mutableMapOf<String, String>(),
        summary = "",
        lane = "",
        loadKg = 0.0,
        batchSize = 0,
        pinned = false,
        amount = 0.0,
        status = BrokerClaimStatus.REQUESTED,
    )
}
