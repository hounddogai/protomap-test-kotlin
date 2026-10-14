package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped scorecard policy calculation. */
interface CappedScorecardPolicyModifier {
    fun describe(): String
    fun adjust(item: CappedScorecardPolicy, value: Double): Double
}

data class ChannelCappedScorecardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedScorecardPolicyModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: CappedScorecardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedScorecardPolicyAdjuster(var cap: Double) : CappedScorecardPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedScorecardPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedScorecardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedScorecardPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedScorecardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedScorecardPolicyAdjuster(var step: Double) : CappedScorecardPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedScorecardPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the capped scorecard policy steps in order and keeps a trace of each. */
data class CappedScorecardPolicyEngine(
    var steps: MutableList<CappedScorecardPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedScorecardPolicy): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedScorecardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedScorecardPolicyEngine(flags: Registry): CappedScorecardPolicyEngine {
    return CappedScorecardPolicyEngine(
        steps = mutableListOf<CappedScorecardPolicyModifier>(
            ChannelCappedScorecardPolicyAdjuster(mutableMapOf<String, Double>("west" to 1.24)),
            CapCappedScorecardPolicyAdjuster(31354.0),
            FlaggedCappedScorecardPolicyAdjuster(flags, "capped-scorecard-policy-weighted", -9.5),
            RoundingCappedScorecardPolicyAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 61248.5,
    )
}
