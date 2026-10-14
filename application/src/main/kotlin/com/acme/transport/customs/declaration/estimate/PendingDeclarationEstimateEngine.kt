package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending declaration estimate calculation. */
interface PendingDeclarationEstimateStep {
    fun describe(): String
    fun adjust(item: PendingDeclarationEstimate, value: Double): Double
}

data class CapPendingDeclarationEstimateAdjuster(var cap: Double) : PendingDeclarationEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDeclarationEstimate, value: Double): Double = minOf(value, cap)
}

data class CountryPendingDeclarationEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDeclarationEstimateStep {
    override fun describe(): String = "by country"

    override fun adjust(item: PendingDeclarationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingDeclarationEstimateLoadKgOffsetAdjuster(var weight: Double) : PendingDeclarationEstimateStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: PendingDeclarationEstimate, value: Double): Double = value + item.loadKg * weight
}

data class RoundingPendingDeclarationEstimateAdjuster(var step: Double) : PendingDeclarationEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDeclarationEstimate, value: Double): Double = roundTo(value, step)
}

data class TieredPendingDeclarationEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDeclarationEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDeclarationEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingDeclarationEstimateAdjuster(var minimum: Double) : PendingDeclarationEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDeclarationEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending declaration estimate steps in order and keeps a trace of each. */
data class PendingDeclarationEstimateEngine(
    var steps: MutableList<PendingDeclarationEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingDeclarationEstimate): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDeclarationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingDeclarationEstimateEngine(): PendingDeclarationEstimateEngine {
    return PendingDeclarationEstimateEngine(
        steps = mutableListOf<PendingDeclarationEstimateStep>(
            CapPendingDeclarationEstimateAdjuster(23527.0),
            CountryPendingDeclarationEstimateAdjuster(
                mutableMapOf<String, Double>("online" to 1.15, "west" to 0.76, "central" to 1.25),
            ),
            PendingDeclarationEstimateLoadKgOffsetAdjuster(1.08),
            RoundingPendingDeclarationEstimateAdjuster(0.5),
            TieredPendingDeclarationEstimateAdjuster(
                mutableListOf<Double>(458.0, 975.0, 3748.0, 9586.0),
                mutableListOf<Double>(0.92, 0.99, 0.91, 0.8),
            ),
            MinimumPendingDeclarationEstimateAdjuster(48.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 92935.3,
    )
}
