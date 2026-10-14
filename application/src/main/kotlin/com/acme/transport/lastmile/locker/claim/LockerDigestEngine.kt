package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.flags.Registry

/** One step of the locker digest calculation. */
interface LockerDigestStep {
    fun describe(): String
    fun adjust(item: LockerDigest, value: Double): Double
}

data class LaneLockerDigestAdjuster(var factors: MutableMap<String, Double>) : LockerDigestStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: LockerDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLockerDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLockerDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLockerDigestAdjuster(var minimum: Double) : LockerDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the locker digest steps in order and keeps a trace of each. */
data class LockerDigestEngine(
    var steps: MutableList<LockerDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerDigest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerDigestEngine(flags: Registry): LockerDigestEngine {
    return LockerDigestEngine(
        steps = mutableListOf<LockerDigestStep>(
            LaneLockerDigestAdjuster(
                mutableMapOf<String, Double>("central" to 1.39, "online" to 1.0, "north" to 1.02),
            ),
            TieredLockerDigestAdjuster(
                mutableListOf<Double>(1773.0, 3226.0, 4397.0, 5071.0),
                mutableListOf<Double>(0.99, 0.85, 0.98, 1.14),
            ),
            FlaggedLockerDigestAdjuster(flags, "locker-digest-lenient", -7.8),
            MinimumLockerDigestAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 64649.6,
    )
}
