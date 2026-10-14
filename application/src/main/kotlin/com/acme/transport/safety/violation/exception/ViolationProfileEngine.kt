package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the violation profile calculation. */
interface ViolationProfileFactor {
    fun describe(): String
    fun adjust(item: ViolationProfile, value: Double): Double
}

data class RoundingViolationProfileAdjuster(var step: Double) : ViolationProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationProfile, value: Double): Double = roundTo(value, step)
}

data class ViolationProfilePalletsOffsetAdjuster(var weight: Double) : ViolationProfileFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ViolationProfile, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class FlaggedViolationProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapViolationProfileAdjuster(var cap: Double) : ViolationProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the violation profile steps in order and keeps a trace of each. */
data class ViolationProfileEngine(
    var steps: MutableList<ViolationProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationProfile): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationProfileFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationProfileEngine(flags: Registry): ViolationProfileEngine {
    return ViolationProfileEngine(
        steps = mutableListOf<ViolationProfileFactor>(
            RoundingViolationProfileAdjuster(0.25),
            ViolationProfilePalletsOffsetAdjuster(0.27),
            FlaggedViolationProfileAdjuster(flags, "violation-profile-scheduled", -1.7),
            CapViolationProfileAdjuster(35511.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 73160.9,
    )
}
