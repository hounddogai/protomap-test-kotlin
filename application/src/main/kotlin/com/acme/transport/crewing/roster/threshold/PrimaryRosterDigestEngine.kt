package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary roster digest calculation. */
interface PrimaryRosterDigestStep {
    fun describe(): String
    fun adjust(item: PrimaryRosterDigest, value: Double): Double
}

data class NamePrimaryRosterDigestAdjuster(var factors: MutableMap<String, Double>) : PrimaryRosterDigestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryRosterDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryRosterDigestAdjuster(var percent: Double) : PrimaryRosterDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRosterDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPrimaryRosterDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryRosterDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryRosterDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryRosterDigestAdjuster(var step: Double) : PrimaryRosterDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRosterDigest, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryRosterDigestAdjuster(var cap: Double) : PrimaryRosterDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryRosterDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the primary roster digest steps in order and keeps a trace of each. */
data class PrimaryRosterDigestEngine(
    var steps: MutableList<PrimaryRosterDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryRosterDigest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryRosterDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryRosterDigestEngine(flags: Registry): PrimaryRosterDigestEngine {
    return PrimaryRosterDigestEngine(
        steps = mutableListOf<PrimaryRosterDigestStep>(
            NamePrimaryRosterDigestAdjuster(mutableMapOf<String, Double>("south" to 1.38, "west" to 0.98)),
            PercentPrimaryRosterDigestAdjuster(-4.7),
            FlaggedPrimaryRosterDigestAdjuster(flags, "primary-roster-digest-pending", 8.5),
            RoundingPrimaryRosterDigestAdjuster(5.0),
            CapPrimaryRosterDigestAdjuster(35110.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 2456.5,
    )
}
