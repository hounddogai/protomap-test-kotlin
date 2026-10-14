package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break slot is in its life cycle. */
enum class RestBreakSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rest break slot. Its code is unique within a tenant. */
data class RestBreakSlot(
    var code: String,
    var name: String,
    var notes: String,
    var driverId: String,
    var discount: Double,
    var carrier: String,
    var capacity: Int,
    var lane: String,
    var unit: String,
    var priority: Int,
    var status: RestBreakSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakSlotStatus): Boolean {
        when (status) {
            RestBreakSlotStatus.DRAFT -> return next == RestBreakSlotStatus.CLOSED
            RestBreakSlotStatus.PENDING -> {
                when (next) {
                    RestBreakSlotStatus.ACTIVE, RestBreakSlotStatus.SUSPENDED -> return true
                }
                return false
            }
            RestBreakSlotStatus.ACTIVE -> return next == RestBreakSlotStatus.CLOSED
            RestBreakSlotStatus.SUSPENDED -> {
                when (next) {
                    RestBreakSlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakSlotStatus) {
        if (!canMoveTo(next)) {
            error("rest break slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(discount * priority.toDouble() * 100) / 100

    fun snapshot(): RestBreakSlot {
        return RestBreakSlot(
            code = code,
            name = name,
            notes = notes,
            driverId = driverId,
            discount = discount,
            carrier = carrier,
            capacity = capacity,
            lane = lane,
            unit = unit,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rest break slot in status draft. */
fun newRestBreakSlot(code: String, name: String): RestBreakSlot {
    return RestBreakSlot(
        code = code,
        name = name,
        notes = "",
        driverId = "",
        discount = 0.0,
        carrier = "",
        capacity = 0,
        lane = "",
        unit = "",
        priority = 0,
        status = RestBreakSlotStatus.DRAFT,
    )
}
