package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break mapping calculation. */
interface RestBreakMappingStep {
    fun describe(): String
    fun adjust(item: RestBreakMapping, value: Double): Double
}

data class RoundingRestBreakMappingAdjuster(var step: Double) : RestBreakMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakMapping, value: Double): Double = roundTo(value, step)
}

data class RestBreakMappingPriorityOffsetAdjuster(var weight: Double) : RestBreakMappingStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: RestBreakMapping, value: Double): Double = value + item.priority.toDouble() * weight
}

data class FlaggedRestBreakMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRestBreakMappingAdjuster(var minimum: Double) : RestBreakMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRestBreakMappingAdjuster(var percent: Double) : RestBreakMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotRestBreakMappingAdjuster(var factors: MutableMap<String, Double>) : RestBreakMappingStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: RestBreakMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rest break mapping steps in order and keeps a trace of each. */
data class RestBreakMappingEngine(
    var steps: MutableList<RestBreakMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakMapping): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakMappingEngine(flags: Registry): RestBreakMappingEngine {
    return RestBreakMappingEngine(
        steps = mutableListOf<RestBreakMappingStep>(
            RoundingRestBreakMappingAdjuster(0.25),
            RestBreakMappingPriorityOffsetAdjuster(1.58),
            FlaggedRestBreakMappingAdjuster(flags, "rest-break-mapping-strict", 6.5),
            MinimumRestBreakMappingAdjuster(49.1),
            PercentRestBreakMappingAdjuster(-12.3),
            DepotRestBreakMappingAdjuster(
                mutableMapOf<String, Double>("central" to 1.18, "east" to 0.72, "north" to 1.33),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 75372.9,
    )
}
