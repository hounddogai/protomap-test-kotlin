package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub agreement calculation. */
interface HubAgreementFactor {
    fun describe(): String
    fun adjust(item: HubAgreement, value: Double): Double
}

data class FlaggedHubAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentHubAgreementAdjuster(var percent: Double) : HubAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapHubAgreementAdjuster(var cap: Double) : HubAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubAgreement, value: Double): Double = minOf(value, cap)
}

data class SegmentHubAgreementAdjuster(var factors: MutableMap<String, Double>) : HubAgreementFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: HubAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredHubAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class HubAgreementAmountOffsetAdjuster(var weight: Double) : HubAgreementFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: HubAgreement, value: Double): Double = value + item.amount * weight
}

/** Runs the hub agreement steps in order and keeps a trace of each. */
data class HubAgreementEngine(
    var steps: MutableList<HubAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubAgreement): Double {
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

    fun evaluateAll(items: MutableList<HubAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubAgreementEngine(flags: Registry): HubAgreementEngine {
    return HubAgreementEngine(
        steps = mutableListOf<HubAgreementFactor>(
            FlaggedHubAgreementAdjuster(flags, "hub-agreement-primary", 8.2),
            PercentHubAgreementAdjuster(9.3),
            CapHubAgreementAdjuster(28553.0),
            SegmentHubAgreementAdjuster(mutableMapOf<String, Double>("south" to 0.93)),
            TieredHubAgreementAdjuster(mutableListOf<Double>(3737.0, 6933.0), mutableListOf<Double>(1.08, 0.85)),
            HubAgreementAmountOffsetAdjuster(0.79),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 84561.0,
    )
}
