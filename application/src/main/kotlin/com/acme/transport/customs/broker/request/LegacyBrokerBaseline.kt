package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy broker baseline is in its life cycle. */
enum class LegacyBrokerBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy broker baseline and its current status. */
data class LegacyBrokerBaseline(
    var code: String,
    var name: String,
    var discount: Double,
    var cost: Double,
    var dock: String,
    var region: String,
    var tags: MutableList<String>,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var status: LegacyBrokerBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBrokerBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBrokerBaselineStatus): Boolean {
        when (status) {
            LegacyBrokerBaselineStatus.REQUESTED -> {
                when (next) {
                    LegacyBrokerBaselineStatus.GRANTED -> return true
                }
                return false
            }
            LegacyBrokerBaselineStatus.GRANTED -> return next == LegacyBrokerBaselineStatus.REVOKED || next == LegacyBrokerBaselineStatus.EXPIRED
            LegacyBrokerBaselineStatus.REVOKED -> return next == LegacyBrokerBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyBrokerBaselineStatus) {
        if (!canMoveTo(next)) {
            error("legacy broker baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((quantity.toDouble() + cost) * 100) / 100

    fun snapshot(): LegacyBrokerBaseline {
        return LegacyBrokerBaseline(
            code = code,
            name = name,
            discount = discount,
            cost = cost,
            dock = dock,
            region = region,
            tags = tags,
            attributes = attributes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy broker baseline in status requested. */
fun newLegacyBrokerBaseline(code: String, name: String): LegacyBrokerBaseline {
    return LegacyBrokerBaseline(
        code = code,
        name = name,
        discount = 0.0,
        cost = 0.0,
        dock = "",
        region = "",
        tags = mutableListOf<String>(),
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        status = LegacyBrokerBaselineStatus.REQUESTED,
    )
}
