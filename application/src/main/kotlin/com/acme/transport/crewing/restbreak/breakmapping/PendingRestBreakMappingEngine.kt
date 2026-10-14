package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending rest break mapping calculation. */
interface PendingRestBreakMappingModifier {
    fun describe(): String
    fun adjust(item: PendingRestBreakMapping, value: Double): Double
}

data class RoundingPendingRestBreakMappingAdjuster(var step: Double) : PendingRestBreakMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRestBreakMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingRestBreakMappingAdjuster(var minimum: Double) : PendingRestBreakMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRestBreakMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitlePendingRestBreakMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRestBreakMappingModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRestBreakMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingRestBreakMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRestBreakMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRestBreakMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingRestBreakMappingAdjuster(var percent: Double) : PendingRestBreakMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRestBreakMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingRestBreakMappingThresholdOffsetAdjuster(
    var weight: Double,
) : PendingRestBreakMappingModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PendingRestBreakMapping, value: Double): Double = value + item.threshold * weight
}

/** Runs the pending rest break mapping steps in order and keeps a trace of each. */
data class PendingRestBreakMappingEngine(
    var steps: MutableList<PendingRestBreakMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRestBreakMapping): Double {
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

    fun evaluateAll(items: MutableList<PendingRestBreakMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRestBreakMappingEngine(flags: Registry): PendingRestBreakMappingEngine {
    return PendingRestBreakMappingEngine(
        steps = mutableListOf<PendingRestBreakMappingModifier>(
            RoundingPendingRestBreakMappingAdjuster(0.05),
            MinimumPendingRestBreakMappingAdjuster(28.4),
            TitlePendingRestBreakMappingAdjuster(
                mutableMapOf<String, Double>("online" to 0.96, "central" to 0.84),
            ),
            FlaggedPendingRestBreakMappingAdjuster(flags, "pending-rest-break-mapping-strict", 2.7),
            PercentPendingRestBreakMappingAdjuster(-18.1),
            PendingRestBreakMappingThresholdOffsetAdjuster(0.63),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 40262.5,
    )
}
