package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial allocation calculation. */
interface AccessorialAllocationFactor {
    fun describe(): String
    fun adjust(item: AccessorialAllocation, value: Double): Double
}

data class AccessorialAllocationVersionOffsetAdjuster(var weight: Double) : AccessorialAllocationFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: AccessorialAllocation, value: Double): Double = value + item.version.toDouble() * weight
}

data class RoundingAccessorialAllocationAdjuster(var step: Double) : AccessorialAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedAccessorialAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAccessorialAllocationAdjuster(var minimum: Double) : AccessorialAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial allocation steps in order and keeps a trace of each. */
data class AccessorialAllocationEngine(
    var steps: MutableList<AccessorialAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AccessorialAllocation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAccessorialAllocationEngine(flags: Registry): AccessorialAllocationEngine {
    return AccessorialAllocationEngine(
        steps = mutableListOf<AccessorialAllocationFactor>(
            AccessorialAllocationVersionOffsetAdjuster(1.58),
            RoundingAccessorialAllocationAdjuster(5.0),
            FlaggedAccessorialAllocationAdjuster(flags, "accessorial-allocation-seasonal", 8.9),
            MinimumAccessorialAllocationAdjuster(21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 43521.4,
    )
}
