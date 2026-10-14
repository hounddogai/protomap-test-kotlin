package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default relay tier is in its life cycle. */
enum class DefaultRelayTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default relay tier, with what the planners need to act on it. */
data class DefaultRelayTier(
    var code: String,
    var name: String,
    var windowDays: Int,
    var trailerId: String,
    var batchSize: Int,
    var summary: String,
    var discount: Double,
    var status: DefaultRelayTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRelayTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRelayTierStatus): Boolean {
        when (status) {
            DefaultRelayTierStatus.DRAFT -> return next == DefaultRelayTierStatus.PENDING
            DefaultRelayTierStatus.PENDING -> return next == DefaultRelayTierStatus.ACTIVE || next == DefaultRelayTierStatus.CLOSED
            DefaultRelayTierStatus.ACTIVE -> return next == DefaultRelayTierStatus.SUSPENDED || next == DefaultRelayTierStatus.CLOSED
            DefaultRelayTierStatus.SUSPENDED -> {
                when (next) {
                    DefaultRelayTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRelayTierStatus) {
        if (!canMoveTo(next)) {
            error("default relay tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round((windowDays.toDouble() + discount) * 100) / 100

    fun snapshot(): DefaultRelayTier {
        return DefaultRelayTier(
            code = code,
            name = name,
            windowDays = windowDays,
            trailerId = trailerId,
            batchSize = batchSize,
            summary = summary,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default relay tier in status draft. */
fun newDefaultRelayTier(code: String, name: String): DefaultRelayTier {
    return DefaultRelayTier(
        code = code,
        name = name,
        windowDays = 0,
        trailerId = "",
        batchSize = 0,
        summary = "",
        discount = 0.0,
        status = DefaultRelayTierStatus.DRAFT,
    )
}
