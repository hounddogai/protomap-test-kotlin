package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled sortation adjustment records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a scheduled sortation adjustment is in its life cycle. */
enum class ScheduledSortationAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled sortation adjustment and its current status. */
data class ScheduledSortationAdjustment(
    var code: String,
    var title: String,
    var floorValue: Double,
    var carrier: String,
    var currency: String,
    var depot: String,
    var threshold: Double,
    var description: String,
    var checksum: String,
    var quantity: Int,
    var status: ScheduledSortationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSortationAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSortationAdjustmentStatus): Boolean {
        when (status) {
            ScheduledSortationAdjustmentStatus.IDLE -> {
                when (next) {
                    ScheduledSortationAdjustmentStatus.WARMING, ScheduledSortationAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledSortationAdjustmentStatus.WARMING -> {
                when (next) {
                    ScheduledSortationAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledSortationAdjustmentStatus.READY -> return next == ScheduledSortationAdjustmentStatus.DRAINING || next == ScheduledSortationAdjustmentStatus.STOPPED
            ScheduledSortationAdjustmentStatus.DRAINING -> {
                when (next) {
                    ScheduledSortationAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSortationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled sortation adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round(floorValue * quantity.toDouble() * 100) / 100

    fun snapshot(): ScheduledSortationAdjustment {
        return ScheduledSortationAdjustment(
            code = code,
            title = title,
            floorValue = floorValue,
            carrier = carrier,
            currency = currency,
            depot = depot,
            threshold = threshold,
            description = description,
            checksum = checksum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled sortation adjustment in status idle. */
fun newScheduledSortationAdjustment(code: String, title: String): ScheduledSortationAdjustment {
    return ScheduledSortationAdjustment(
        code = code,
        title = title,
        floorValue = 0.0,
        carrier = "",
        currency = "",
        depot = "",
        threshold = 0.0,
        description = "",
        checksum = "",
        quantity = 0,
        status = ScheduledSortationAdjustmentStatus.IDLE,
    )
}
