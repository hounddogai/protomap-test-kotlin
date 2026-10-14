package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling broker slot records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a rolling broker slot is in its life cycle. */
enum class RollingBrokerSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling broker slot. Its code is unique within a tenant. */
data class RollingBrokerSlot(
    var code: String,
    var name: String,
    var trailerId: String,
    var depot: String,
    var createdAt: Int,
    var expiresAt: Int,
    var limit: Int,
    var threshold: Double,
    var endsAt: Int,
    var status: RollingBrokerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBrokerSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBrokerSlotStatus): Boolean {
        when (status) {
            RollingBrokerSlotStatus.OPEN -> return next == RollingBrokerSlotStatus.IN_REVIEW
            RollingBrokerSlotStatus.IN_REVIEW -> {
                when (next) {
                    RollingBrokerSlotStatus.RESOLVED -> return true
                }
                return false
            }
            RollingBrokerSlotStatus.RESOLVED -> {
                when (next) {
                    RollingBrokerSlotStatus.REOPENED, RollingBrokerSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingBrokerSlotStatus.REOPENED -> {
                when (next) {
                    RollingBrokerSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingBrokerSlotStatus) {
        if (!canMoveTo(next)) {
            error("rolling broker slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round(threshold * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RollingBrokerSlot {
        return RollingBrokerSlot(
            code = code,
            name = name,
            trailerId = trailerId,
            depot = depot,
            createdAt = createdAt,
            expiresAt = expiresAt,
            limit = limit,
            threshold = threshold,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling broker slot in status open. */
fun newRollingBrokerSlot(code: String, name: String): RollingBrokerSlot {
    return RollingBrokerSlot(
        code = code,
        name = name,
        trailerId = "",
        depot = "",
        createdAt = 0,
        expiresAt = 0,
        limit = 0,
        threshold = 0.0,
        endsAt = 0,
        status = RollingBrokerSlotStatus.OPEN,
    )
}
