package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped duty period policy calculation. */
interface CappedDutyPeriodPolicyFactor {
    fun describe(): String
    fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double
}

data class RoundingCappedDutyPeriodPolicyAdjuster(var step: Double) : CappedDutyPeriodPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedDutyPeriodPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDutyPeriodPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedDutyPeriodPolicyPalletsOffsetAdjuster(var weight: Double) : CappedDutyPeriodPolicyFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class TieredCappedDutyPeriodPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDutyPeriodPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedDutyPeriodPolicyAdjuster(var percent: Double) : CappedDutyPeriodPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedDutyPeriodPolicyAdjuster(var minimum: Double) : CappedDutyPeriodPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDutyPeriodPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped duty period policy steps in order and keeps a trace of each. */
data class CappedDutyPeriodPolicyEngine(
    var steps: MutableList<CappedDutyPeriodPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDutyPeriodPolicy): Double {
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

    fun evaluateAll(items: MutableList<CappedDutyPeriodPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDutyPeriodPolicyEngine(flags: Registry): CappedDutyPeriodPolicyEngine {
    return CappedDutyPeriodPolicyEngine(
        steps = mutableListOf<CappedDutyPeriodPolicyFactor>(
            RoundingCappedDutyPeriodPolicyAdjuster(0.5),
            FlaggedCappedDutyPeriodPolicyAdjuster(flags, "capped-duty-period-policy-legacy", 2.5),
            CappedDutyPeriodPolicyPalletsOffsetAdjuster(1.37),
            TieredCappedDutyPeriodPolicyAdjuster(
                mutableListOf<Double>(659.0, 956.0, 6061.0),
                mutableListOf<Double>(1.04, 1.14, 1.03),
            ),
            PercentCappedDutyPeriodPolicyAdjuster(-10.2),
            MinimumCappedDutyPeriodPolicyAdjuster(43.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 94102.2,
    )
}
