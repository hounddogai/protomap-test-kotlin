package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity rollup records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity rollup is in its life cycle. */
enum class CapacityRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capacity rollup, with what the planners need to act on it. */
data class CapacityRollup(
    var code: String,
    var label: String,
    var threshold: Double,
    var updatedAt: Int,
    var active: Boolean,
    var minimum: Double,
    var floorValue: Double,
    var status: CapacityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityRollupStatus): Boolean {
        when (status) {
            CapacityRollupStatus.NEW -> {
                when (next) {
                    CapacityRollupStatus.RUNNING, CapacityRollupStatus.FAILED -> return true
                }
                return false
            }
            CapacityRollupStatus.QUEUED -> {
                when (next) {
                    CapacityRollupStatus.RUNNING, CapacityRollupStatus.FAILED -> return true
                }
                return false
            }
            CapacityRollupStatus.RUNNING -> {
                when (next) {
                    CapacityRollupStatus.DONE, CapacityRollupStatus.FAILED -> return true
                }
                return false
            }
            CapacityRollupStatus.DONE -> {
                when (next) {
                    CapacityRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityRollupStatus) {
        if (!canMoveTo(next)) {
            error("capacity rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((floorValue - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CapacityRollup {
        return CapacityRollup(
            code = code,
            label = label,
            threshold = threshold,
            updatedAt = updatedAt,
            active = active,
            minimum = minimum,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity rollup in status new. */
fun newCapacityRollup(code: String, label: String): CapacityRollup {
    return CapacityRollup(
        code = code,
        label = label,
        threshold = 0.0,
        updatedAt = 0,
        active = false,
        minimum = 0.0,
        floorValue = 0.0,
        status = CapacityRollupStatus.NEW,
    )
}
