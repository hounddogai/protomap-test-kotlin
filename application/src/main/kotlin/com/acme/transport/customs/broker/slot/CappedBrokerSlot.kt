package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped broker slot records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a capped broker slot is in its life cycle. */
enum class CappedBrokerSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped broker slot, with what the planners need to act on it. */
data class CappedBrokerSlot(
    var code: String,
    var name: String,
    var depot: String,
    var trailerId: String,
    var discount: Double,
    var notes: String,
    var segment: String,
    var priority: Int,
    var region: String,
    var status: CappedBrokerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBrokerSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBrokerSlotStatus): Boolean {
        when (status) {
            CappedBrokerSlotStatus.DRAFT -> {
                when (next) {
                    CappedBrokerSlotStatus.PENDING -> return true
                }
                return false
            }
            CappedBrokerSlotStatus.PENDING -> {
                when (next) {
                    CappedBrokerSlotStatus.ACTIVE -> return true
                }
                return false
            }
            CappedBrokerSlotStatus.ACTIVE -> return next == CappedBrokerSlotStatus.SUSPENDED
            CappedBrokerSlotStatus.SUSPENDED -> return next == CappedBrokerSlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedBrokerSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped broker slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((discount - priority.toDouble()) * 100) / 100

    fun snapshot(): CappedBrokerSlot {
        return CappedBrokerSlot(
            code = code,
            name = name,
            depot = depot,
            trailerId = trailerId,
            discount = discount,
            notes = notes,
            segment = segment,
            priority = priority,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped broker slot in status draft. */
fun newCappedBrokerSlot(code: String, name: String): CappedBrokerSlot {
    return CappedBrokerSlot(
        code = code,
        name = name,
        depot = "",
        trailerId = "",
        discount = 0.0,
        notes = "",
        segment = "",
        priority = 0,
        region = "",
        status = CappedBrokerSlotStatus.DRAFT,
    )
}
