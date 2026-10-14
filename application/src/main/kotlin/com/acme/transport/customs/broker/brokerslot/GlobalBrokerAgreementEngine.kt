package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global broker agreement calculation. */
interface GlobalBrokerAgreementFactor {
    fun describe(): String
    fun adjust(item: GlobalBrokerAgreement, value: Double): Double
}

data class PercentGlobalBrokerAgreementAdjuster(var percent: Double) : GlobalBrokerAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalBrokerAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalBrokerAgreementAdjuster(var minimum: Double) : GlobalBrokerAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalBrokerAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalBrokerAgreementAdjuster(var step: Double) : GlobalBrokerAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalBrokerAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalBrokerAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalBrokerAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalBrokerAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalBrokerAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalBrokerAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalBrokerAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalBrokerAgreementAdjuster(var cap: Double) : GlobalBrokerAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalBrokerAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the global broker agreement steps in order and keeps a trace of each. */
data class GlobalBrokerAgreementEngine(
    var steps: MutableList<GlobalBrokerAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalBrokerAgreement): Double {
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

    fun evaluateAll(items: MutableList<GlobalBrokerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalBrokerAgreementEngine(flags: Registry): GlobalBrokerAgreementEngine {
    return GlobalBrokerAgreementEngine(
        steps = mutableListOf<GlobalBrokerAgreementFactor>(
            PercentGlobalBrokerAgreementAdjuster(-23.5),
            MinimumGlobalBrokerAgreementAdjuster(23.9),
            RoundingGlobalBrokerAgreementAdjuster(0.5),
            FlaggedGlobalBrokerAgreementAdjuster(flags, "global-broker-agreement-regional", -9.8),
            TieredGlobalBrokerAgreementAdjuster(
                mutableListOf<Double>(8937.0, 9232.0),
                mutableListOf<Double>(1.0, 1.02),
            ),
            CapGlobalBrokerAgreementAdjuster(10097.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 68925.3,
    )
}
