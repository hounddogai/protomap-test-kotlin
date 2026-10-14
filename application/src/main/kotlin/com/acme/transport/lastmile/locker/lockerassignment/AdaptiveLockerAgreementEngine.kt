package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive locker agreement calculation. */
interface AdaptiveLockerAgreementModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLockerAgreement, value: Double): Double
}

data class TieredAdaptiveLockerAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLockerAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLockerAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameAdaptiveLockerAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLockerAgreementModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveLockerAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveLockerAgreementAdjuster(var cap: Double) : AdaptiveLockerAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLockerAgreement, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveLockerAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLockerAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLockerAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveLockerAgreementAdjuster(var minimum: Double) : AdaptiveLockerAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLockerAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive locker agreement steps in order and keeps a trace of each. */
data class AdaptiveLockerAgreementEngine(
    var steps: MutableList<AdaptiveLockerAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveLockerAgreement): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveLockerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveLockerAgreementEngine(flags: Registry): AdaptiveLockerAgreementEngine {
    return AdaptiveLockerAgreementEngine(
        steps = mutableListOf<AdaptiveLockerAgreementModifier>(
            TieredAdaptiveLockerAgreementAdjuster(
                mutableListOf<Double>(1837.0, 2548.0, 8004.0, 9673.0),
                mutableListOf<Double>(0.9, 1.16, 1.2, 0.91),
            ),
            NameAdaptiveLockerAgreementAdjuster(mutableMapOf<String, Double>("north" to 0.79)),
            CapAdaptiveLockerAgreementAdjuster(31247.0),
            FlaggedAdaptiveLockerAgreementAdjuster(flags, "adaptive-locker-agreement-lenient", 5.2),
            MinimumAdaptiveLockerAgreementAdjuster(4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 19173.1,
    )
}
