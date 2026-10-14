package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped swap policy calculation. */
interface CappedSwapPolicyAdjuster {
    fun describe(): String
    fun adjust(item: CappedSwapPolicy, value: Double): Double
}

data class CapCappedSwapPolicyAdjuster(var cap: Double) : CappedSwapPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedSwapPolicy, value: Double): Double = minOf(value, cap)
}

data class CappedSwapPolicyStartsAtOffsetAdjuster(var weight: Double) : CappedSwapPolicyAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: CappedSwapPolicy, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class TitleCappedSwapPolicyAdjuster(var factors: MutableMap<String, Double>) : CappedSwapPolicyAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedSwapPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedSwapPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedSwapPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedSwapPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedSwapPolicyAdjuster(var step: Double) : CappedSwapPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedSwapPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedSwapPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedSwapPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedSwapPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped swap policy steps in order and keeps a trace of each. */
data class CappedSwapPolicyEngine(
    var steps: MutableList<CappedSwapPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedSwapPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedSwapPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedSwapPolicyEngine(flags: Registry): CappedSwapPolicyEngine {
    return CappedSwapPolicyEngine(
        steps = mutableListOf<CappedSwapPolicyAdjuster>(
            CapCappedSwapPolicyAdjuster(35754.0),
            CappedSwapPolicyStartsAtOffsetAdjuster(1.31),
            TitleCappedSwapPolicyAdjuster(mutableMapOf<String, Double>("north" to 0.85)),
            TieredCappedSwapPolicyAdjuster(
                mutableListOf<Double>(2847.0, 9200.0),
                mutableListOf<Double>(0.97, 0.95),
            ),
            RoundingCappedSwapPolicyAdjuster(0.5),
            FlaggedCappedSwapPolicyAdjuster(flags, "capped-swap-policy-rolling", 10.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 99422.2,
    )
}
