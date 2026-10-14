package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the regional violation digest calculation. */
interface RegionalViolationDigestAdjuster {
    fun describe(): String
    fun adjust(item: RegionalViolationDigest, value: Double): Double
}

data class CapRegionalViolationDigestAdjuster(var cap: Double) : RegionalViolationDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalViolationDigest, value: Double): Double = minOf(value, cap)
}

data class SegmentRegionalViolationDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalViolationDigestAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: RegionalViolationDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalViolationDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalViolationDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalViolationDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalViolationDigestRetriesOffsetAdjuster(var weight: Double) : RegionalViolationDigestAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RegionalViolationDigest, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the regional violation digest steps in order and keeps a trace of each. */
data class RegionalViolationDigestEngine(
    var steps: MutableList<RegionalViolationDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalViolationDigest): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalViolationDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalViolationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalViolationDigestEngine(flags: Registry): RegionalViolationDigestEngine {
    return RegionalViolationDigestEngine(
        steps = mutableListOf<RegionalViolationDigestAdjuster>(
            CapRegionalViolationDigestAdjuster(5044.0),
            SegmentRegionalViolationDigestAdjuster(mutableMapOf<String, Double>("online" to 1.4)),
            FlaggedRegionalViolationDigestAdjuster(flags, "regional-violation-digest-weighted", 8.9),
            RegionalViolationDigestRetriesOffsetAdjuster(0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 2500.3,
    )
}
