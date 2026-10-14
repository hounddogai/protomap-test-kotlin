package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional broker checkpoint is in its life cycle. */
enum class RegionalBrokerCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional broker checkpoint, with what the planners need to act on it. */
data class RegionalBrokerCheckpoint(
    var code: String,
    var label: String,
    var region: String,
    var rate: Double,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var routeCode: String,
    var status: RegionalBrokerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBrokerCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBrokerCheckpointStatus): Boolean {
        when (status) {
            RegionalBrokerCheckpointStatus.DRAFT -> {
                when (next) {
                    RegionalBrokerCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalBrokerCheckpointStatus.PENDING -> return next == RegionalBrokerCheckpointStatus.SUSPENDED
            RegionalBrokerCheckpointStatus.ACTIVE -> {
                when (next) {
                    RegionalBrokerCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            RegionalBrokerCheckpointStatus.SUSPENDED -> return next == RegionalBrokerCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalBrokerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("regional broker checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((startsAt.toDouble() + rate) * 100) / 100

    fun snapshot(): RegionalBrokerCheckpoint {
        return RegionalBrokerCheckpoint(
            code = code,
            label = label,
            region = region,
            rate = rate,
            unit = unit,
            attributes = attributes,
            startsAt = startsAt,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional broker checkpoint in status draft. */
fun newRegionalBrokerCheckpoint(code: String, label: String): RegionalBrokerCheckpoint {
    return RegionalBrokerCheckpoint(
        code = code,
        label = label,
        region = "",
        rate = 0.0,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        routeCode = "",
        status = RegionalBrokerCheckpointStatus.DRAFT,
    )
}
