package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dwell estimate calculation. */
interface DwellEstimateAdjuster {
    fun describe(): String
    fun adjust(item: DwellEstimate, value: Double): Double
}

data class FlaggedDwellEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DwellEstimateFloorValueOffsetAdjuster(var weight: Double) : DwellEstimateAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DwellEstimate, value: Double): Double = value + item.floorValue * weight
}

data class RegionDwellEstimateAdjuster(var factors: MutableMap<String, Double>) : DwellEstimateAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: DwellEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDwellEstimateAdjuster(var minimum: Double) : DwellEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDwellEstimateAdjuster(var percent: Double) : DwellEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dwell estimate steps in order and keeps a trace of each. */
data class DwellEstimateEngine(
    var steps: MutableList<DwellEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellEstimate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellEstimateEngine(flags: Registry): DwellEstimateEngine {
    return DwellEstimateEngine(
        steps = mutableListOf<DwellEstimateAdjuster>(
            FlaggedDwellEstimateAdjuster(flags, "dwell-estimate-capped", -5.6),
            DwellEstimateFloorValueOffsetAdjuster(0.64),
            RegionDwellEstimateAdjuster(mutableMapOf<String, Double>("north" to 0.97, "west" to 1.36)),
            MinimumDwellEstimateAdjuster(49.6),
            PercentDwellEstimateAdjuster(8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 5233.9,
    )
}
