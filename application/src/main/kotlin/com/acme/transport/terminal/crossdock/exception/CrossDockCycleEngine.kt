package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cross dock cycle calculation. */
interface CrossDockCycleModifier {
    fun describe(): String
    fun adjust(item: CrossDockCycle, value: Double): Double
}

data class CrossDockCycleRateOffsetAdjuster(var weight: Double) : CrossDockCycleModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: CrossDockCycle, value: Double): Double = value + item.rate * weight
}

data class PercentCrossDockCycleAdjuster(var percent: Double) : CrossDockCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCrossDockCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the cross dock cycle steps in order and keeps a trace of each. */
data class CrossDockCycleEngine(
    var steps: MutableList<CrossDockCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockCycleEngine(flags: Registry): CrossDockCycleEngine {
    return CrossDockCycleEngine(
        steps = mutableListOf<CrossDockCycleModifier>(
            CrossDockCycleRateOffsetAdjuster(1.38),
            PercentCrossDockCycleAdjuster(-19.5),
            FlaggedCrossDockCycleAdjuster(flags, "cross-dock-cycle-default", -5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 27400.2,
    )
}
