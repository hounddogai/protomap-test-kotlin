package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback capacity preference calculation. */
interface FallbackCapacityPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: FallbackCapacityPreference, value: Double): Double
}

data class FallbackCapacityPreferenceStartsAtOffsetAdjuster(
    var weight: Double,
) : FallbackCapacityPreferenceAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FallbackCapacityPreference, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class DepotFallbackCapacityPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCapacityPreferenceAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackCapacityPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackCapacityPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCapacityPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCapacityPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackCapacityPreferenceAdjuster(var minimum: Double) : FallbackCapacityPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCapacityPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackCapacityPreferenceAdjuster(var cap: Double) : FallbackCapacityPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCapacityPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackCapacityPreferenceAdjuster(var step: Double) : FallbackCapacityPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCapacityPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback capacity preference steps in order and keeps a trace of each. */
data class FallbackCapacityPreferenceEngine(
    var steps: MutableList<FallbackCapacityPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackCapacityPreference): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCapacityPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackCapacityPreferenceEngine(flags: Registry): FallbackCapacityPreferenceEngine {
    return FallbackCapacityPreferenceEngine(
        steps = mutableListOf<FallbackCapacityPreferenceAdjuster>(
            FallbackCapacityPreferenceStartsAtOffsetAdjuster(1.58),
            DepotFallbackCapacityPreferenceAdjuster(mutableMapOf<String, Double>("north" to 1.09)),
            FlaggedFallbackCapacityPreferenceAdjuster(flags, "fallback-capacity-preference-capped", 1.6),
            MinimumFallbackCapacityPreferenceAdjuster(37.2),
            CapFallbackCapacityPreferenceAdjuster(28126.0),
            RoundingFallbackCapacityPreferenceAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 86624.4,
    )
}
