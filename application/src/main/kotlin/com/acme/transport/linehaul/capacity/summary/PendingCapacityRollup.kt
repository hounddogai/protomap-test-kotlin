package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending capacity rollup is in its life cycle. */
enum class PendingCapacityRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a pending capacity rollup and its current status. */
data class PendingCapacityRollup(
    var code: String,
    var title: String,
    var visible: Boolean,
    var priority: Int,
    var active: Boolean,
    var margin: Double,
    var pallets: Int,
    var graceDays: Int,
    var windowDays: Int,
    var cost: Double,
    var status: PendingCapacityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCapacityRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCapacityRollupStatus): Boolean {
        when (status) {
            PendingCapacityRollupStatus.IDLE -> {
                when (next) {
                    PendingCapacityRollupStatus.WARMING -> return true
                }
                return false
            }
            PendingCapacityRollupStatus.WARMING -> {
                when (next) {
                    PendingCapacityRollupStatus.READY -> return true
                }
                return false
            }
            PendingCapacityRollupStatus.READY -> {
                when (next) {
                    PendingCapacityRollupStatus.DRAINING -> return true
                }
                return false
            }
            PendingCapacityRollupStatus.DRAINING -> {
                when (next) {
                    PendingCapacityRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCapacityRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending capacity rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((margin - priority.toDouble()) * 100) / 100

    fun snapshot(): PendingCapacityRollup {
        return PendingCapacityRollup(
            code = code,
            title = title,
            visible = visible,
            priority = priority,
            active = active,
            margin = margin,
            pallets = pallets,
            graceDays = graceDays,
            windowDays = windowDays,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending capacity rollup in status idle. */
fun newPendingCapacityRollup(code: String, title: String): PendingCapacityRollup {
    return PendingCapacityRollup(
        code = code,
        title = title,
        visible = false,
        priority = 0,
        active = false,
        margin = 0.0,
        pallets = 0,
        graceDays = 0,
        windowDays = 0,
        cost = 0.0,
        status = PendingCapacityRollupStatus.IDLE,
    )
}
