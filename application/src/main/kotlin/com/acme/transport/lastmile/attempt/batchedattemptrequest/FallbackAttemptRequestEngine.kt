package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback attempt request calculation. */
interface FallbackAttemptRequestAdjuster {
    fun describe(): String
    fun adjust(item: FallbackAttemptRequest, value: Double): Double
}

data class MinimumFallbackAttemptRequestAdjuster(var minimum: Double) : FallbackAttemptRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAttemptRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackAttemptRequestAdjuster(var cap: Double) : FallbackAttemptRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAttemptRequest, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackAttemptRequestAdjuster(var percent: Double) : FallbackAttemptRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAttemptRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback attempt request steps in order and keeps a trace of each. */
data class FallbackAttemptRequestEngine(
    var steps: MutableList<FallbackAttemptRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackAttemptRequest): Double {
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

    fun evaluateAll(items: MutableList<FallbackAttemptRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackAttemptRequestEngine(): FallbackAttemptRequestEngine {
    return FallbackAttemptRequestEngine(
        steps = mutableListOf<FallbackAttemptRequestAdjuster>(
            MinimumFallbackAttemptRequestAdjuster(10.9),
            CapFallbackAttemptRequestAdjuster(21906.0),
            PercentFallbackAttemptRequestAdjuster(10.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 56636.4,
    )
}
