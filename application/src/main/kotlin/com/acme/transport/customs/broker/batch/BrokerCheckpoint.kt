package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker checkpoint is in its life cycle. */
enum class BrokerCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One broker checkpoint, with what the planners need to act on it. */
data class BrokerCheckpoint(
    var code: String,
    var name: String,
    var maximum: Double,
    var rate: Double,
    var contactEmail: String,
    var distanceKm: Double,
    var externalRef: String,
    var budget: Double,
    var capacity: Int,
    var routeCode: String,
    var status: BrokerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerCheckpointStatus): Boolean {
        when (status) {
            BrokerCheckpointStatus.PROPOSED -> return next == BrokerCheckpointStatus.APPROVED
            BrokerCheckpointStatus.APPROVED -> return next == BrokerCheckpointStatus.REJECTED || next == BrokerCheckpointStatus.WITHDRAWN
            BrokerCheckpointStatus.REJECTED -> {
                when (next) {
                    BrokerCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("broker checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((budget + maximum) * 100) / 100

    fun snapshot(): BrokerCheckpoint {
        return BrokerCheckpoint(
            code = code,
            name = name,
            maximum = maximum,
            rate = rate,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            externalRef = externalRef,
            budget = budget,
            capacity = capacity,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a broker checkpoint in status proposed. */
fun newBrokerCheckpoint(code: String, name: String): BrokerCheckpoint {
    return BrokerCheckpoint(
        code = code,
        name = name,
        maximum = 0.0,
        rate = 0.0,
        contactEmail = "",
        distanceKm = 0.0,
        externalRef = "",
        budget = 0.0,
        capacity = 0,
        routeCode = "",
        status = BrokerCheckpointStatus.PROPOSED,
    )
}
