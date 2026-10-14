package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient dispute policy calculation. */
interface LenientDisputePolicyModifier {
    fun describe(): String
    fun adjust(item: LenientDisputePolicy, value: Double): Double
}

data class RoundingLenientDisputePolicyAdjuster(var step: Double) : LenientDisputePolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDisputePolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientDisputePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDisputePolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDisputePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientDisputePolicyBudgetOffsetAdjuster(var weight: Double) : LenientDisputePolicyModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: LenientDisputePolicy, value: Double): Double = value + item.budget * weight
}

/** Runs the lenient dispute policy steps in order and keeps a trace of each. */
data class LenientDisputePolicyEngine(
    var steps: MutableList<LenientDisputePolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientDisputePolicy): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDisputePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientDisputePolicyEngine(flags: Registry): LenientDisputePolicyEngine {
    return LenientDisputePolicyEngine(
        steps = mutableListOf<LenientDisputePolicyModifier>(
            RoundingLenientDisputePolicyAdjuster(0.5),
            FlaggedLenientDisputePolicyAdjuster(flags, "lenient-dispute-policy-scheduled", 8.0),
            LenientDisputePolicyBudgetOffsetAdjuster(0.79),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 76761.2,
    )
}
