package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone policy records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone policy is in its life cycle. */
enum class ZonePolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A zone policy as the zone team keeps it. */
data class ZonePolicy(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var budget: Double,
    var graceDays: Int,
    var depot: String,
    var currency: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var status: ZonePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZonePolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZonePolicyStatus): Boolean {
        when (status) {
            ZonePolicyStatus.DRAFT -> return next == ZonePolicyStatus.SUSPENDED || next == ZonePolicyStatus.CLOSED
            ZonePolicyStatus.PENDING -> return next == ZonePolicyStatus.SUSPENDED || next == ZonePolicyStatus.CLOSED
            ZonePolicyStatus.ACTIVE -> return next == ZonePolicyStatus.SUSPENDED
            ZonePolicyStatus.SUSPENDED -> return next == ZonePolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ZonePolicyStatus) {
        if (!canMoveTo(next)) {
            error("zone policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

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

    fun netTotal(): Double = round(graceDays.toDouble() * budget * 100) / 100

    fun snapshot(): ZonePolicy {
        return ZonePolicy(
            code = code,
            name = name,
            tags = tags,
            budget = budget,
            graceDays = graceDays,
            depot = depot,
            currency = currency,
            region = region,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a zone policy in status draft. */
fun newZonePolicy(code: String, name: String): ZonePolicy {
    return ZonePolicy(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        budget = 0.0,
        graceDays = 0,
        depot = "",
        currency = "",
        region = "",
        attributes = mutableMapOf<String, String>(),
        status = ZonePolicyStatus.DRAFT,
    )
}
