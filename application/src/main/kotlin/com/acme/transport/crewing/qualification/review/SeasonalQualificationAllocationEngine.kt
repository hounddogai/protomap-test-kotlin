package com.acme.transport.crewing.qualification.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal qualification allocation calculation. */
interface SeasonalQualificationAllocationAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalQualificationAllocation, value: Double): Double
}

data class MinimumSeasonalQualificationAllocationAdjuster(
    var minimum: Double,
) : SeasonalQualificationAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalQualificationAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalQualificationAllocationAdjuster(
    var cap: Double,
) : SeasonalQualificationAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalQualificationAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalQualificationAllocationAdjuster(
    var step: Double,
) : SeasonalQualificationAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalQualificationAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalQualificationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalQualificationAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalQualificationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalQualificationAllocationCeilingOffsetAdjuster(
    var weight: Double,
) : SeasonalQualificationAllocationAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: SeasonalQualificationAllocation, value: Double): Double = value + item.ceiling * weight
}

/** Runs the seasonal qualification allocation steps in order and keeps a trace of each. */
data class SeasonalQualificationAllocationEngine(
    var steps: MutableList<SeasonalQualificationAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalQualificationAllocation): Double {
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

    fun evaluateAll(items: MutableList<SeasonalQualificationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalQualificationAllocationEngine(flags: Registry): SeasonalQualificationAllocationEngine {
    return SeasonalQualificationAllocationEngine(
        steps = mutableListOf<SeasonalQualificationAllocationAdjuster>(
            MinimumSeasonalQualificationAllocationAdjuster(37.4),
            CapSeasonalQualificationAllocationAdjuster(14504.0),
            RoundingSeasonalQualificationAllocationAdjuster(0.5),
            FlaggedSeasonalQualificationAllocationAdjuster(
                flags,
                "seasonal-qualification-allocation-global",
                -8.9,
            ),
            SeasonalQualificationAllocationCeilingOffsetAdjuster(0.07),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 79559.1,
    )
}
