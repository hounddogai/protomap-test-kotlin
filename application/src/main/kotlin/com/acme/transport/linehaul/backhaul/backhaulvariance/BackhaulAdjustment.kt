package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul adjustment is in its life cycle. */
enum class BackhaulAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One backhaul adjustment, with what the planners need to act on it. */
data class BackhaulAdjustment(
    var code: String,
    var name: String,
    var ceiling: Double,
    var quantity: Int,
    var unit: String,
    var lane: String,
    var minimum: Double,
    var priority: Int,
    var stopCount: Int,
    var rate: Double,
    var floorValue: Double,
    var status: BackhaulAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulAdjustmentStatus): Boolean {
        when (status) {
            BackhaulAdjustmentStatus.NEW -> return next == BackhaulAdjustmentStatus.RUNNING || next == BackhaulAdjustmentStatus.DONE
            BackhaulAdjustmentStatus.QUEUED -> return next == BackhaulAdjustmentStatus.RUNNING || next == BackhaulAdjustmentStatus.DONE
            BackhaulAdjustmentStatus.RUNNING -> return next == BackhaulAdjustmentStatus.FAILED
            BackhaulAdjustmentStatus.DONE -> return next == BackhaulAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BackhaulAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("backhaul adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round(quantity.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): BackhaulAdjustment {
        return BackhaulAdjustment(
            code = code,
            name = name,
            ceiling = ceiling,
            quantity = quantity,
            unit = unit,
            lane = lane,
            minimum = minimum,
            priority = priority,
            stopCount = stopCount,
            rate = rate,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a backhaul adjustment in status new. */
fun newBackhaulAdjustment(code: String, name: String): BackhaulAdjustment {
    return BackhaulAdjustment(
        code = code,
        name = name,
        ceiling = 0.0,
        quantity = 0,
        unit = "",
        lane = "",
        minimum = 0.0,
        priority = 0,
        stopCount = 0,
        rate = 0.0,
        floorValue = 0.0,
        status = BackhaulAdjustmentStatus.NEW,
    )
}
