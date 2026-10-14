package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual qualification profile calculation. */
interface ManualQualificationProfileModifier {
    fun describe(): String
    fun adjust(item: ManualQualificationProfile, value: Double): Double
}

data class PercentManualQualificationProfileAdjuster(var percent: Double) : ManualQualificationProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualQualificationProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualQualificationProfileAdjuster(var cap: Double) : ManualQualificationProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualQualificationProfile, value: Double): Double = minOf(value, cap)
}

data class MinimumManualQualificationProfileAdjuster(var minimum: Double) : ManualQualificationProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualQualificationProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualQualificationProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualQualificationProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualQualificationProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualQualificationProfileFuelLitersOffsetAdjuster(
    var weight: Double,
) : ManualQualificationProfileModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: ManualQualificationProfile, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the manual qualification profile steps in order and keeps a trace of each. */
data class ManualQualificationProfileEngine(
    var steps: MutableList<ManualQualificationProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualQualificationProfile): Double {
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

    fun evaluateAll(items: MutableList<ManualQualificationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualQualificationProfileEngine(flags: Registry): ManualQualificationProfileEngine {
    return ManualQualificationProfileEngine(
        steps = mutableListOf<ManualQualificationProfileModifier>(
            PercentManualQualificationProfileAdjuster(8.3),
            CapManualQualificationProfileAdjuster(36945.0),
            MinimumManualQualificationProfileAdjuster(40.9),
            FlaggedManualQualificationProfileAdjuster(flags, "manual-qualification-profile-scheduled", -0.9),
            ManualQualificationProfileFuelLitersOffsetAdjuster(1.81),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 44665.9,
    )
}
