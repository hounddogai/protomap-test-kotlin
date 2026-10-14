package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop adjustment is in its life cycle. */
enum class DropAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A drop adjustment. Its code is unique within a tenant. */
data class DropAdjustment(
    var code: String,
    var title: String,
    var summary: String,
    var floorValue: Double,
    var stopCount: Int,
    var priority: Int,
    var status: DropAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropAdjustmentStatus): Boolean {
        when (status) {
            DropAdjustmentStatus.IDLE -> {
                when (next) {
                    DropAdjustmentStatus.DRAINING, DropAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            DropAdjustmentStatus.WARMING -> {
                when (next) {
                    DropAdjustmentStatus.READY, DropAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            DropAdjustmentStatus.READY -> {
                when (next) {
                    DropAdjustmentStatus.DRAINING, DropAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            DropAdjustmentStatus.DRAINING -> {
                when (next) {
                    DropAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("drop adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((floorValue - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DropAdjustment {
        return DropAdjustment(
            code = code,
            title = title,
            summary = summary,
            floorValue = floorValue,
            stopCount = stopCount,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a drop adjustment in status idle. */
fun newDropAdjustment(code: String, title: String): DropAdjustment {
    return DropAdjustment(
        code = code,
        title = title,
        summary = "",
        floorValue = 0.0,
        stopCount = 0,
        priority = 0,
        status = DropAdjustmentStatus.IDLE,
    )
}
