package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback capacity request calculation. */
interface FallbackCapacityRequestFactor {
    fun describe(): String
    fun adjust(item: FallbackCapacityRequest, value: Double): Double
}

data class TieredFallbackCapacityRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCapacityRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCapacityRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackCapacityRequestAdjuster(var step: Double) : FallbackCapacityRequestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCapacityRequest, value: Double): Double = roundTo(value, step)
}

data class FallbackCapacityRequestCeilingOffsetAdjuster(var weight: Double) : FallbackCapacityRequestFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: FallbackCapacityRequest, value: Double): Double = value + item.ceiling * weight
}

/** Runs the fallback capacity request steps in order and keeps a trace of each. */
data class FallbackCapacityRequestEngine(
    var steps: MutableList<FallbackCapacityRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackCapacityRequest): Double {
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

    fun evaluateAll(items: MutableList<FallbackCapacityRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackCapacityRequestEngine(): FallbackCapacityRequestEngine {
    return FallbackCapacityRequestEngine(
        steps = mutableListOf<FallbackCapacityRequestFactor>(
            TieredFallbackCapacityRequestAdjuster(
                mutableListOf<Double>(4526.0, 5035.0, 5935.0, 8606.0),
                mutableListOf<Double>(1.17, 1.0, 0.94, 1.07),
            ),
            RoundingFallbackCapacityRequestAdjuster(5.0),
            FallbackCapacityRequestCeilingOffsetAdjuster(1.37),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 89244.2,
    )
}
