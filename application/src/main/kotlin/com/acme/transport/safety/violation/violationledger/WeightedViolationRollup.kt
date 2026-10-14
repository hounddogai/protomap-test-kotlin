package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted violation rollup is in its life cycle. */
enum class WeightedViolationRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted violation rollup as the violation team keeps it. */
data class WeightedViolationRollup(
    var code: String,
    var name: String,
    var phone: String,
    var ownerId: String,
    var priority: Int,
    var etaMinutes: Int,
    var active: Boolean,
    var floorValue: Double,
    var visible: Boolean,
    var dock: String,
    var status: WeightedViolationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedViolationRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedViolationRollupStatus): Boolean {
        when (status) {
            WeightedViolationRollupStatus.NEW -> return next == WeightedViolationRollupStatus.RUNNING
            WeightedViolationRollupStatus.QUEUED -> {
                when (next) {
                    WeightedViolationRollupStatus.RUNNING, WeightedViolationRollupStatus.DONE -> return true
                }
                return false
            }
            WeightedViolationRollupStatus.RUNNING -> return next == WeightedViolationRollupStatus.DONE
            WeightedViolationRollupStatus.DONE -> {
                when (next) {
                    WeightedViolationRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedViolationRollupStatus) {
        if (!canMoveTo(next)) {
            error("weighted violation rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((priority.toDouble() - floorValue) * 100) / 100

    fun snapshot(): WeightedViolationRollup {
        return WeightedViolationRollup(
            code = code,
            name = name,
            phone = phone,
            ownerId = ownerId,
            priority = priority,
            etaMinutes = etaMinutes,
            active = active,
            floorValue = floorValue,
            visible = visible,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted violation rollup in status new. */
fun newWeightedViolationRollup(code: String, name: String): WeightedViolationRollup {
    return WeightedViolationRollup(
        code = code,
        name = name,
        phone = "",
        ownerId = "",
        priority = 0,
        etaMinutes = 0,
        active = false,
        floorValue = 0.0,
        visible = false,
        dock = "",
        status = WeightedViolationRollupStatus.NEW,
    )
}
