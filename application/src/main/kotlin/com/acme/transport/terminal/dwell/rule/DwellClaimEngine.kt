package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dwell claim calculation. */
interface DwellClaimAdjuster {
    fun describe(): String
    fun adjust(item: DwellClaim, value: Double): Double
}

data class MinimumDwellClaimAdjuster(var minimum: Double) : DwellClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDwellClaimAdjuster(var percent: Double) : DwellClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDwellClaimAdjuster(var cap: Double) : DwellClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the dwell claim steps in order and keeps a trace of each. */
data class DwellClaimEngine(
    var steps: MutableList<DwellClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellClaim): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellClaimEngine(): DwellClaimEngine {
    return DwellClaimEngine(
        steps = mutableListOf<DwellClaimAdjuster>(
            MinimumDwellClaimAdjuster(10.1),
            PercentDwellClaimAdjuster(22.0),
            CapDwellClaimAdjuster(4650.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 12664.9,
    )
}
