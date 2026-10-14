package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial profile calculation. */
interface AccessorialProfileStep {
    fun describe(): String
    fun adjust(item: AccessorialProfile, value: Double): Double
}

data class PercentAccessorialProfileAdjuster(var percent: Double) : AccessorialProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAccessorialProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameAccessorialProfileAdjuster(var factors: MutableMap<String, Double>) : AccessorialProfileStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AccessorialProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the accessorial profile steps in order and keeps a trace of each. */
data class AccessorialProfileEngine(
    var steps: MutableList<AccessorialProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialProfile): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialProfileEngine(): AccessorialProfileEngine {
    return AccessorialProfileEngine(
        steps = mutableListOf<AccessorialProfileStep>(
            PercentAccessorialProfileAdjuster(-2.5),
            TieredAccessorialProfileAdjuster(
                mutableListOf<Double>(42.0, 1666.0, 4679.0, 5421.0),
                mutableListOf<Double>(0.91, 0.93, 0.8, 0.95),
            ),
            NameAccessorialProfileAdjuster(mutableMapOf<String, Double>("central" to 1.24)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 53626.2,
    )
}
