package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the locker policy calculation. */
interface LockerPolicyStep {
    fun describe(): String
    fun adjust(item: LockerPolicy, value: Double): Double
}

data class CapLockerPolicyAdjuster(var cap: Double) : LockerPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerPolicy, value: Double): Double = minOf(value, cap)
}

data class PercentLockerPolicyAdjuster(var percent: Double) : LockerPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LockerPolicyQuantityOffsetAdjuster(var weight: Double) : LockerPolicyStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LockerPolicy, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingLockerPolicyAdjuster(var step: Double) : LockerPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedLockerPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleLockerPolicyAdjuster(var factors: MutableMap<String, Double>) : LockerPolicyStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LockerPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the locker policy steps in order and keeps a trace of each. */
data class LockerPolicyEngine(
    var steps: MutableList<LockerPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LockerPolicy): Double {
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

    fun evaluateAll(items: MutableList<LockerPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLockerPolicyEngine(flags: Registry): LockerPolicyEngine {
    return LockerPolicyEngine(
        steps = mutableListOf<LockerPolicyStep>(
            CapLockerPolicyAdjuster(41610.0),
            PercentLockerPolicyAdjuster(-22.8),
            LockerPolicyQuantityOffsetAdjuster(0.92),
            RoundingLockerPolicyAdjuster(0.5),
            FlaggedLockerPolicyAdjuster(flags, "locker-policy-tiered", 7.3),
            TitleLockerPolicyAdjuster(mutableMapOf<String, Double>("east" to 0.7, "online" to 1.1)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 63337.0,
    )
}
