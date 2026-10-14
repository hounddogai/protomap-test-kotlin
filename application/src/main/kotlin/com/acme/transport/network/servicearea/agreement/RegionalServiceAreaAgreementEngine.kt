package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional service area agreement calculation. */
interface RegionalServiceAreaAgreementStep {
    fun describe(): String
    fun adjust(item: RegionalServiceAreaAgreement, value: Double): Double
}

data class PercentRegionalServiceAreaAgreementAdjuster(var percent: Double) : RegionalServiceAreaAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalServiceAreaAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRegionalServiceAreaAgreementAdjuster(var cap: Double) : RegionalServiceAreaAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalServiceAreaAgreement, value: Double): Double = minOf(value, cap)
}

data class RegionalServiceAreaAgreementOdometerOffsetAdjuster(
    var weight: Double,
) : RegionalServiceAreaAgreementStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RegionalServiceAreaAgreement, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class TieredRegionalServiceAreaAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalServiceAreaAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalServiceAreaAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameRegionalServiceAreaAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalServiceAreaAgreementStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalServiceAreaAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional service area agreement steps in order and keeps a trace of each. */
data class RegionalServiceAreaAgreementEngine(
    var steps: MutableList<RegionalServiceAreaAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalServiceAreaAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalServiceAreaAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalServiceAreaAgreementEngine(): RegionalServiceAreaAgreementEngine {
    return RegionalServiceAreaAgreementEngine(
        steps = mutableListOf<RegionalServiceAreaAgreementStep>(
            PercentRegionalServiceAreaAgreementAdjuster(20.0),
            CapRegionalServiceAreaAgreementAdjuster(12424.0),
            RegionalServiceAreaAgreementOdometerOffsetAdjuster(1.65),
            TieredRegionalServiceAreaAgreementAdjuster(
                mutableListOf<Double>(1538.0, 6457.0, 8744.0),
                mutableListOf<Double>(0.93, 0.81, 1.16),
            ),
            NameRegionalServiceAreaAgreementAdjuster(mutableMapOf<String, Double>("central" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 83585.0,
    )
}
