package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the locker profile calculation. */
interface LockerProfileAdjuster {
    fun describe(): String
    fun adjust(item: LockerProfile, value: Double): Double
}

data class RoundingLockerProfileAdjuster(var step: Double) : LockerProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerProfile, value: Double): Double = roundTo(value, step)
}

data class PercentLockerProfileAdjuster(var percent: Double) : LockerProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneLockerProfileAdjuster(var factors: MutableMap<String, Double>) : LockerProfileAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LockerProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLockerProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLockerProfileAdjuster(var cap: Double) : LockerProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerProfile, value: Double): Double = minOf(value, cap)
}

data class LockerProfileVersionOffsetAdjuster(var weight: Double) : LockerProfileAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: LockerProfile, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the locker profile steps in order and keeps a trace of each. */
data class LockerProfileEngine(
    var steps: MutableList<LockerProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerProfile): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LockerProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerProfileEngine(flags: Registry): LockerProfileEngine {
    return LockerProfileEngine(
        steps = mutableListOf<LockerProfileAdjuster>(
            RoundingLockerProfileAdjuster(0.1),
            PercentLockerProfileAdjuster(-19.4),
            LaneLockerProfileAdjuster(mutableMapOf<String, Double>("south" to 1.09, "north" to 0.88)),
            FlaggedLockerProfileAdjuster(flags, "locker-profile-strict", 6.3),
            CapLockerProfileAdjuster(32502.0),
            LockerProfileVersionOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 41396.8,
    )
}
