package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict hold mapping is in its life cycle. */
enum class StrictHoldMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict hold mapping and its current status. */
data class StrictHoldMapping(
    var code: String,
    var title: String,
    var lane: String,
    var source: String,
    var capacity: Int,
    var quantity: Int,
    var dock: String,
    var amount: Double,
    var carrier: String,
    var status: StrictHoldMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictHoldMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictHoldMappingStatus): Boolean {
        when (status) {
            StrictHoldMappingStatus.OPEN -> return next == StrictHoldMappingStatus.REOPENED || next == StrictHoldMappingStatus.ARCHIVED
            StrictHoldMappingStatus.IN_REVIEW -> return next == StrictHoldMappingStatus.REOPENED || next == StrictHoldMappingStatus.ARCHIVED
            StrictHoldMappingStatus.RESOLVED -> return next == StrictHoldMappingStatus.ARCHIVED
            StrictHoldMappingStatus.REOPENED -> {
                when (next) {
                    StrictHoldMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictHoldMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict hold mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round((capacity.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): StrictHoldMapping {
        return StrictHoldMapping(
            code = code,
            title = title,
            lane = lane,
            source = source,
            capacity = capacity,
            quantity = quantity,
            dock = dock,
            amount = amount,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict hold mapping in status open. */
fun newStrictHoldMapping(code: String, title: String): StrictHoldMapping {
    return StrictHoldMapping(
        code = code,
        title = title,
        lane = "",
        source = "",
        capacity = 0,
        quantity = 0,
        dock = "",
        amount = 0.0,
        carrier = "",
        status = StrictHoldMappingStatus.OPEN,
    )
}
