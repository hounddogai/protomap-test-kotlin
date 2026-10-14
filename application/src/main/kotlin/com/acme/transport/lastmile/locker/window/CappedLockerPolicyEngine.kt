package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped locker policy calculation. */
interface CappedLockerPolicyStep {
    fun describe(): String
    fun adjust(item: CappedLockerPolicy, value: Double): Double
}

data class MinimumCappedLockerPolicyAdjuster(var minimum: Double) : CappedLockerPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedLockerPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedLockerPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLockerPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLockerPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceCappedLockerPolicyAdjuster(var factors: MutableMap<String, Double>) : CappedLockerPolicyStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedLockerPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedLockerPolicyAdjuster(var step: Double) : CappedLockerPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedLockerPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedLockerPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedLockerPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedLockerPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedLockerPolicyRetriesOffsetAdjuster(var weight: Double) : CappedLockerPolicyStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: CappedLockerPolicy, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the capped locker policy steps in order and keeps a trace of each. */
data class CappedLockerPolicyEngine(
    var steps: MutableList<CappedLockerPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedLockerPolicy): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedLockerPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedLockerPolicyEngine(flags: Registry): CappedLockerPolicyEngine {
    return CappedLockerPolicyEngine(
        steps = mutableListOf<CappedLockerPolicyStep>(
            MinimumCappedLockerPolicyAdjuster(20.3),
            TieredCappedLockerPolicyAdjuster(
                mutableListOf<Double>(6646.0, 7030.0),
                mutableListOf<Double>(0.88, 0.82),
            ),
            SourceCappedLockerPolicyAdjuster(mutableMapOf<String, Double>("central" to 1.0, "online" to 1.02)),
            RoundingCappedLockerPolicyAdjuster(0.05),
            FlaggedCappedLockerPolicyAdjuster(flags, "capped-locker-policy-strict", 4.3),
            CappedLockerPolicyRetriesOffsetAdjuster(0.61),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 96381.0,
    )
}
