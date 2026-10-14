package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial mapping calculation. */
interface AccessorialMappingStep {
    fun describe(): String
    fun adjust(item: AccessorialMapping, value: Double): Double
}

data class CountryAccessorialMappingAdjuster(var factors: MutableMap<String, Double>) : AccessorialMappingStep {
    override fun describe(): String = "by country"

    override fun adjust(item: AccessorialMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAccessorialMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAccessorialMappingAdjuster(var percent: Double) : AccessorialMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the accessorial mapping steps in order and keeps a trace of each. */
data class AccessorialMappingEngine(
    var steps: MutableList<AccessorialMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialMapping): Double {
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

    fun evaluateAll(items: MutableList<AccessorialMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialMappingEngine(flags: Registry): AccessorialMappingEngine {
    return AccessorialMappingEngine(
        steps = mutableListOf<AccessorialMappingStep>(
            CountryAccessorialMappingAdjuster(mutableMapOf<String, Double>("west" to 1.36)),
            FlaggedAccessorialMappingAdjuster(flags, "accessorial-mapping-seasonal", 6.5),
            PercentAccessorialMappingAdjuster(-14.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 14565.9,
    )
}
