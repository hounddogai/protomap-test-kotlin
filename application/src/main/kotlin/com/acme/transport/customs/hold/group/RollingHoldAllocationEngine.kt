package com.acme.transport.customs.hold.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling hold allocation calculation. */
interface RollingHoldAllocationFactor {
    fun describe(): String
    fun adjust(item: RollingHoldAllocation, value: Double): Double
}

data class RollingHoldAllocationRetriesOffsetAdjuster(var weight: Double) : RollingHoldAllocationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RollingHoldAllocation, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TitleRollingHoldAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingHoldAllocationFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingHoldAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingHoldAllocationAdjuster(var cap: Double) : RollingHoldAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingHoldAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingHoldAllocationAdjuster(var step: Double) : RollingHoldAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingHoldAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingHoldAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingHoldAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingHoldAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling hold allocation steps in order and keeps a trace of each. */
data class RollingHoldAllocationEngine(
    var steps: MutableList<RollingHoldAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingHoldAllocation): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingHoldAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingHoldAllocationEngine(flags: Registry): RollingHoldAllocationEngine {
    return RollingHoldAllocationEngine(
        steps = mutableListOf<RollingHoldAllocationFactor>(
            RollingHoldAllocationRetriesOffsetAdjuster(1.53),
            TitleRollingHoldAllocationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.33, "central" to 1.37),
            ),
            CapRollingHoldAllocationAdjuster(32985.0),
            RoundingRollingHoldAllocationAdjuster(0.1),
            FlaggedRollingHoldAllocationAdjuster(flags, "rolling-hold-allocation-batched", 4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 32988.6,
    )
}
