package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional defect mapping calculation. */
interface RegionalDefectMappingModifier {
    fun describe(): String
    fun adjust(item: RegionalDefectMapping, value: Double): Double
}

data class PercentRegionalDefectMappingAdjuster(var percent: Double) : RegionalDefectMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDefectMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalDefectMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDefectMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDefectMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRegionalDefectMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDefectMappingModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalDefectMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalDefectMappingAdjuster(var minimum: Double) : RegionalDefectMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDefectMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional defect mapping steps in order and keeps a trace of each. */
data class RegionalDefectMappingEngine(
    var steps: MutableList<RegionalDefectMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalDefectMapping): Double {
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

    fun evaluateAll(items: MutableList<RegionalDefectMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalDefectMappingEngine(flags: Registry): RegionalDefectMappingEngine {
    return RegionalDefectMappingEngine(
        steps = mutableListOf<RegionalDefectMappingModifier>(
            PercentRegionalDefectMappingAdjuster(-8.0),
            FlaggedRegionalDefectMappingAdjuster(flags, "regional-defect-mapping-seasonal", -2.6),
            NameRegionalDefectMappingAdjuster(mutableMapOf<String, Double>("east" to 1.25)),
            MinimumRegionalDefectMappingAdjuster(14.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 15514.3,
    )
}
