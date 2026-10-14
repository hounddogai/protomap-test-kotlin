package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial cycle calculation. */
interface AccessorialCycleFactor {
    fun describe(): String
    fun adjust(item: AccessorialCycle, value: Double): Double
}

data class AccessorialCycleFuelLitersOffsetAdjuster(var weight: Double) : AccessorialCycleFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: AccessorialCycle, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedAccessorialCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAccessorialCycleAdjuster(var percent: Double) : AccessorialCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAccessorialCycleAdjuster(var cap: Double) : AccessorialCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialCycle, value: Double): Double = minOf(value, cap)
}

data class MinimumAccessorialCycleAdjuster(var minimum: Double) : AccessorialCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial cycle steps in order and keeps a trace of each. */
data class AccessorialCycleEngine(
    var steps: MutableList<AccessorialCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AccessorialCycle): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAccessorialCycleEngine(flags: Registry): AccessorialCycleEngine {
    return AccessorialCycleEngine(
        steps = mutableListOf<AccessorialCycleFactor>(
            AccessorialCycleFuelLitersOffsetAdjuster(0.59),
            FlaggedAccessorialCycleAdjuster(flags, "accessorial-cycle-pending", -3.6),
            PercentAccessorialCycleAdjuster(-10.6),
            CapAccessorialCycleAdjuster(41431.0),
            MinimumAccessorialCycleAdjuster(13.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 81874.0,
    )
}
