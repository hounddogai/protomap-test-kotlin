package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling service area cycle records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a rolling service area cycle is in its life cycle. */
enum class RollingServiceAreaCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling service area cycle as the service area team keeps it. */
data class RollingServiceAreaCycle(
    var code: String,
    var label: String,
    var ceiling: Double,
    var tags: MutableList<String>,
    var category: String,
    var odometer: Int,
    var description: String,
    var channel: String,
    var sequence: Int,
    var status: RollingServiceAreaCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingServiceAreaCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingServiceAreaCycleStatus): Boolean {
        when (status) {
            RollingServiceAreaCycleStatus.PLANNED -> return next == RollingServiceAreaCycleStatus.SCHEDULED || next == RollingServiceAreaCycleStatus.IN_PROGRESS
            RollingServiceAreaCycleStatus.SCHEDULED -> return next == RollingServiceAreaCycleStatus.COMPLETED
            RollingServiceAreaCycleStatus.IN_PROGRESS -> {
                when (next) {
                    RollingServiceAreaCycleStatus.COMPLETED -> return true
                }
                return false
            }
            RollingServiceAreaCycleStatus.COMPLETED -> {
                when (next) {
                    RollingServiceAreaCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingServiceAreaCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling service area cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling service area cycle"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(sequence.toDouble() * ceiling * 100) / 100

    fun snapshot(): RollingServiceAreaCycle {
        return RollingServiceAreaCycle(
            code = code,
            label = label,
            ceiling = ceiling,
            tags = tags,
            category = category,
            odometer = odometer,
            description = description,
            channel = channel,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling service area cycle in status planned. */
fun newRollingServiceAreaCycle(code: String, label: String): RollingServiceAreaCycle {
    return RollingServiceAreaCycle(
        code = code,
        label = label,
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        category = "",
        odometer = 0,
        description = "",
        channel = "",
        sequence = 0,
        status = RollingServiceAreaCycleStatus.PLANNED,
    )
}
