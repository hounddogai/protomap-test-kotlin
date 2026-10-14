package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.flags.Registry

/** One step of the rolling qualification digest calculation. */
interface RollingQualificationDigestAdjuster {
    fun describe(): String
    fun adjust(item: RollingQualificationDigest, value: Double): Double
}

data class NameRollingQualificationDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingQualificationDigestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingQualificationDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingQualificationDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingQualificationDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingQualificationDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingQualificationDigestAdjuster(var minimum: Double) : RollingQualificationDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingQualificationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingQualificationDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingQualificationDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingQualificationDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling qualification digest steps in order and keeps a trace of each. */
data class RollingQualificationDigestEngine(
    var steps: MutableList<RollingQualificationDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingQualificationDigest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingQualificationDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingQualificationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingQualificationDigestEngine(flags: Registry): RollingQualificationDigestEngine {
    return RollingQualificationDigestEngine(
        steps = mutableListOf<RollingQualificationDigestAdjuster>(
            NameRollingQualificationDigestAdjuster(mutableMapOf<String, Double>("west" to 0.91)),
            TieredRollingQualificationDigestAdjuster(
                mutableListOf<Double>(942.0, 3489.0, 8061.0),
                mutableListOf<Double>(1.04, 0.9, 1.19),
            ),
            MinimumRollingQualificationDigestAdjuster(47.2),
            FlaggedRollingQualificationDigestAdjuster(flags, "rolling-qualification-digest-capped", 1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 90099.9,
    )
}
