package com.acme.transport.customs.hold.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped hold request calculation. */
interface CappedHoldRequestModifier {
    fun describe(): String
    fun adjust(item: CappedHoldRequest, value: Double): Double
}

data class TieredCappedHoldRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHoldRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHoldRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedHoldRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedHoldRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedHoldRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedHoldRequestAdjuster(var step: Double) : CappedHoldRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHoldRequest, value: Double): Double = roundTo(value, step)
}

data class CapCappedHoldRequestAdjuster(var cap: Double) : CappedHoldRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedHoldRequest, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedHoldRequestAdjuster(var minimum: Double) : CappedHoldRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedHoldRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotCappedHoldRequestAdjuster(var factors: MutableMap<String, Double>) : CappedHoldRequestModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: CappedHoldRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped hold request steps in order and keeps a trace of each. */
data class CappedHoldRequestEngine(
    var steps: MutableList<CappedHoldRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedHoldRequest): Double {
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

    fun evaluateAll(items: MutableList<CappedHoldRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedHoldRequestEngine(flags: Registry): CappedHoldRequestEngine {
    return CappedHoldRequestEngine(
        steps = mutableListOf<CappedHoldRequestModifier>(
            TieredCappedHoldRequestAdjuster(
                mutableListOf<Double>(5699.0, 7463.0, 8773.0, 8893.0),
                mutableListOf<Double>(1.08, 1.01, 0.9, 1.11),
            ),
            FlaggedCappedHoldRequestAdjuster(flags, "capped-hold-request-batched", 1.2),
            RoundingCappedHoldRequestAdjuster(1.0),
            CapCappedHoldRequestAdjuster(35823.0),
            MinimumCappedHoldRequestAdjuster(10.4),
            DepotCappedHoldRequestAdjuster(
                mutableMapOf<String, Double>("north" to 1.08, "east" to 0.98, "online" to 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 9617.2,
    )
}
