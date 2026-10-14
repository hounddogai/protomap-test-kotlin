package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback defect variance calculation. */
interface FallbackDefectVarianceFactor {
    fun describe(): String
    fun adjust(item: FallbackDefectVariance, value: Double): Double
}

data class RoundingFallbackDefectVarianceAdjuster(var step: Double) : FallbackDefectVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDefectVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackDefectVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDefectVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDefectVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackDefectVarianceAdjuster(var cap: Double) : FallbackDefectVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDefectVariance, value: Double): Double = minOf(value, cap)
}

data class DepotFallbackDefectVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDefectVarianceFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackDefectVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackDefectVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDefectVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDefectVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackDefectVarianceAdjuster(var minimum: Double) : FallbackDefectVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDefectVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback defect variance steps in order and keeps a trace of each. */
data class FallbackDefectVarianceEngine(
    var steps: MutableList<FallbackDefectVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackDefectVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDefectVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackDefectVarianceEngine(flags: Registry): FallbackDefectVarianceEngine {
    return FallbackDefectVarianceEngine(
        steps = mutableListOf<FallbackDefectVarianceFactor>(
            RoundingFallbackDefectVarianceAdjuster(0.25),
            FlaggedFallbackDefectVarianceAdjuster(flags, "fallback-defect-variance-regional", -6.1),
            CapFallbackDefectVarianceAdjuster(21468.0),
            DepotFallbackDefectVarianceAdjuster(mutableMapOf<String, Double>("online" to 0.99, "south" to 1.15)),
            TieredFallbackDefectVarianceAdjuster(
                mutableListOf<Double>(3467.0, 3881.0, 4701.0, 9230.0),
                mutableListOf<Double>(1.0, 1.15, 0.93, 0.86),
            ),
            MinimumFallbackDefectVarianceAdjuster(16.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 47746.1,
    )
}
