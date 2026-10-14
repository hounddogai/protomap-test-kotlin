package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay group records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay group is in its life cycle. */
enum class RelayGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a relay group and its current status. */
data class RelayGroup(
    var code: String,
    var label: String,
    var locale: String,
    var description: String,
    var attributes: MutableMap<String, String>,
    var priority: Int,
    var depot: String,
    var stopCount: Int,
    var carrier: String,
    var expiresAt: Int,
    var status: RelayGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayGroupStatus): Boolean {
        when (status) {
            RelayGroupStatus.CREATED -> return next == RelayGroupStatus.VALIDATED || next == RelayGroupStatus.PUBLISHED
            RelayGroupStatus.VALIDATED -> return next == RelayGroupStatus.PUBLISHED
            RelayGroupStatus.PUBLISHED -> return next == RelayGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RelayGroupStatus) {
        if (!canMoveTo(next)) {
            error("relay group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"relay group"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((expiresAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RelayGroup {
        return RelayGroup(
            code = code,
            label = label,
            locale = locale,
            description = description,
            attributes = attributes,
            priority = priority,
            depot = depot,
            stopCount = stopCount,
            carrier = carrier,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a relay group in status created. */
fun newRelayGroup(code: String, label: String): RelayGroup {
    return RelayGroup(
        code = code,
        label = label,
        locale = "",
        description = "",
        attributes = mutableMapOf<String, String>(),
        priority = 0,
        depot = "",
        stopCount = 0,
        carrier = "",
        expiresAt = 0,
        status = RelayGroupStatus.CREATED,
    )
}
