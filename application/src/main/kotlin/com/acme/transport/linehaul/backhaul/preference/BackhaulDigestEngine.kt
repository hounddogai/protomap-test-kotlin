package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul digest calculation. */
interface BackhaulDigestStep {
    fun describe(): String
    fun adjust(item: BackhaulDigest, value: Double): Double
}

data class MinimumBackhaulDigestAdjuster(var minimum: Double) : BackhaulDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBackhaulDigestAdjuster(var percent: Double) : BackhaulDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBackhaulDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the backhaul digest steps in order and keeps a trace of each. */
data class BackhaulDigestEngine(
    var steps: MutableList<BackhaulDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulDigest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulDigestEngine(flags: Registry): BackhaulDigestEngine {
    return BackhaulDigestEngine(
        steps = mutableListOf<BackhaulDigestStep>(
            MinimumBackhaulDigestAdjuster(31.6),
            PercentBackhaulDigestAdjuster(18.8),
            FlaggedBackhaulDigestAdjuster(flags, "backhaul-digest-lenient", 3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 19212.3,
    )
}
