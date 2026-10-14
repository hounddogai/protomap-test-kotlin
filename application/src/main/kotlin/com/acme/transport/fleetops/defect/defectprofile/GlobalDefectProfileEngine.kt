package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global defect profile calculation. */
interface GlobalDefectProfileModifier {
    fun describe(): String
    fun adjust(item: GlobalDefectProfile, value: Double): Double
}

data class CapGlobalDefectProfileAdjuster(var cap: Double) : GlobalDefectProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDefectProfile, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalDefectProfileAdjuster(var minimum: Double) : GlobalDefectProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDefectProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameGlobalDefectProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDefectProfileModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalDefectProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalDefectProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDefectProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDefectProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentGlobalDefectProfileAdjuster(var percent: Double) : GlobalDefectProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDefectProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global defect profile steps in order and keeps a trace of each. */
data class GlobalDefectProfileEngine(
    var steps: MutableList<GlobalDefectProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalDefectProfile): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalDefectProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalDefectProfileEngine(flags: Registry): GlobalDefectProfileEngine {
    return GlobalDefectProfileEngine(
        steps = mutableListOf<GlobalDefectProfileModifier>(
            CapGlobalDefectProfileAdjuster(48703.0),
            MinimumGlobalDefectProfileAdjuster(28.0),
            NameGlobalDefectProfileAdjuster(mutableMapOf<String, Double>("east" to 1.22, "south" to 1.01)),
            FlaggedGlobalDefectProfileAdjuster(flags, "global-defect-profile-capped", -8.2),
            PercentGlobalDefectProfileAdjuster(18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 35236.1,
    )
}
