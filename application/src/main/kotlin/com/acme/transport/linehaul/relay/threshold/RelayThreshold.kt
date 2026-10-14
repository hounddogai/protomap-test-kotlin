package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay threshold records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay threshold is in its life cycle. */
enum class RelayThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One relay threshold, with what the planners need to act on it. */
data class RelayThreshold(
    var code: String,
    var title: String,
    var minimum: Double,
    var description: String,
    var routeCode: String,
    var score: Double,
    var status: RelayThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayThresholdStatus): Boolean {
        when (status) {
            RelayThresholdStatus.IDLE -> return next == RelayThresholdStatus.READY || next == RelayThresholdStatus.STOPPED
            RelayThresholdStatus.WARMING -> return next == RelayThresholdStatus.READY || next == RelayThresholdStatus.DRAINING
            RelayThresholdStatus.READY -> return next == RelayThresholdStatus.DRAINING
            RelayThresholdStatus.DRAINING -> {
                when (next) {
                    RelayThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayThresholdStatus) {
        if (!canMoveTo(next)) {
            error("relay threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"relay threshold"} $code"

    fun weightedLoad(): Double = round((minimum - score) * 100) / 100

    fun snapshot(): RelayThreshold {
        return RelayThreshold(
            code = code,
            title = title,
            minimum = minimum,
            description = description,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay threshold in status idle. */
fun newRelayThreshold(code: String, title: String): RelayThreshold {
    return RelayThreshold(
        code = code,
        title = title,
        minimum = 0.0,
        description = "",
        routeCode = "",
        score = 0.0,
        status = RelayThresholdStatus.IDLE,
    )
}
