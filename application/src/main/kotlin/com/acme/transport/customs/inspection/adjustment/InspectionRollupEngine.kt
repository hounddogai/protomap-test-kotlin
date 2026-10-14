package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection rollup calculation. */
interface InspectionRollupStep {
    fun describe(): String
    fun adjust(item: InspectionRollup, value: Double): Double
}

data class CapInspectionRollupAdjuster(var cap: Double) : InspectionRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionRollup, value: Double): Double = minOf(value, cap)
}

data class FlaggedInspectionRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumInspectionRollupAdjuster(var minimum: Double) : InspectionRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionInspectionRollupAdjuster(var factors: MutableMap<String, Double>) : InspectionRollupStep {
    override fun describe(): String = "by region"

    override fun adjust(item: InspectionRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection rollup steps in order and keeps a trace of each. */
data class InspectionRollupEngine(
    var steps: MutableList<InspectionRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionRollup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionRollupEngine(flags: Registry): InspectionRollupEngine {
    return InspectionRollupEngine(
        steps = mutableListOf<InspectionRollupStep>(
            CapInspectionRollupAdjuster(1987.0),
            FlaggedInspectionRollupAdjuster(flags, "inspection-rollup-adaptive", -0.9),
            MinimumInspectionRollupAdjuster(41.5),
            RegionInspectionRollupAdjuster(
                mutableMapOf<String, Double>("west" to 0.85, "north" to 0.85, "online" to 1.38),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 59522.2,
    )
}
