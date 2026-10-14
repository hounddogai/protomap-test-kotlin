package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop claim calculation. */
interface DropClaimFactor {
    fun describe(): String
    fun adjust(item: DropClaim, value: Double): Double
}

data class RoundingDropClaimAdjuster(var step: Double) : DropClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropClaim, value: Double): Double = roundTo(value, step)
}

data class PercentDropClaimAdjuster(var percent: Double) : DropClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDropClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the drop claim steps in order and keeps a trace of each. */
data class DropClaimEngine(
    var steps: MutableList<DropClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropClaim): Double {
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

    fun evaluateAll(items: MutableList<DropClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropClaimEngine(): DropClaimEngine {
    return DropClaimEngine(
        steps = mutableListOf<DropClaimFactor>(
            RoundingDropClaimAdjuster(0.25),
            PercentDropClaimAdjuster(-19.5),
            TieredDropClaimAdjuster(
                mutableListOf<Double>(3164.0, 4703.0, 7408.0, 8722.0),
                mutableListOf<Double>(1.1, 0.91, 0.9, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 97585.4,
    )
}
