package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect profile calculation. */
interface DefectProfileStep {
    fun describe(): String
    fun adjust(item: DefectProfile, value: Double): Double
}

data class TieredDefectProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefectProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefectProfileMaximumOffsetAdjuster(var weight: Double) : DefectProfileStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DefectProfile, value: Double): Double = value + item.maximum * weight
}

/** Runs the defect profile steps in order and keeps a trace of each. */
data class DefectProfileEngine(
    var steps: MutableList<DefectProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectProfile): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectProfileEngine(flags: Registry): DefectProfileEngine {
    return DefectProfileEngine(
        steps = mutableListOf<DefectProfileStep>(
            TieredDefectProfileAdjuster(
                mutableListOf<Double>(23.0, 100.0, 2304.0, 9814.0),
                mutableListOf<Double>(1.1, 1.0, 1.07, 1.03),
            ),
            FlaggedDefectProfileAdjuster(flags, "defect-profile-adaptive", -4.5),
            DefectProfileMaximumOffsetAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 42923.2,
    )
}
