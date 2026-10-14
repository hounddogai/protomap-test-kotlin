package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped hub request calculation. */
interface CappedHubRequestAdjuster {
    fun describe(): String
    fun adjust(item: CappedHubRequest, value: Double): Double
}

data class TieredCappedHubRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHubRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHubRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedHubRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedHubRequestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedHubRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelCappedHubRequestAdjuster(var factors: MutableMap<String, Double>) : CappedHubRequestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedHubRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedHubRequestBudgetOffsetAdjuster(var weight: Double) : CappedHubRequestAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CappedHubRequest, value: Double): Double = value + item.budget * weight
}

data class MinimumCappedHubRequestAdjuster(var minimum: Double) : CappedHubRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedHubRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedHubRequestAdjuster(var step: Double) : CappedHubRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHubRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the capped hub request steps in order and keeps a trace of each. */
data class CappedHubRequestEngine(
    var steps: MutableList<CappedHubRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedHubRequest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedHubRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedHubRequestEngine(flags: Registry): CappedHubRequestEngine {
    return CappedHubRequestEngine(
        steps = mutableListOf<CappedHubRequestAdjuster>(
            TieredCappedHubRequestAdjuster(
                mutableListOf<Double>(155.0, 5294.0, 8801.0),
                mutableListOf<Double>(1.1, 1.17, 1.17),
            ),
            FlaggedCappedHubRequestAdjuster(flags, "capped-hub-request-fallback", 1.9),
            LabelCappedHubRequestAdjuster(mutableMapOf<String, Double>("west" to 0.98, "retail" to 0.78)),
            CappedHubRequestBudgetOffsetAdjuster(0.08),
            MinimumCappedHubRequestAdjuster(11.4),
            RoundingCappedHubRequestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 78228.1,
    )
}
