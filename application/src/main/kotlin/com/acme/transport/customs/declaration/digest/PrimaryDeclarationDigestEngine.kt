package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary declaration digest calculation. */
interface PrimaryDeclarationDigestAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDeclarationDigest, value: Double): Double
}

data class FlaggedPrimaryDeclarationDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDeclarationDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDeclarationDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryDeclarationDigestAdjuster(var percent: Double) : PrimaryDeclarationDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDeclarationDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryDeclarationDigestAdjuster(var minimum: Double) : PrimaryDeclarationDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDeclarationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary declaration digest steps in order and keeps a trace of each. */
data class PrimaryDeclarationDigestEngine(
    var steps: MutableList<PrimaryDeclarationDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryDeclarationDigest): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDeclarationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryDeclarationDigestEngine(flags: Registry): PrimaryDeclarationDigestEngine {
    return PrimaryDeclarationDigestEngine(
        steps = mutableListOf<PrimaryDeclarationDigestAdjuster>(
            FlaggedPrimaryDeclarationDigestAdjuster(flags, "primary-declaration-digest-fallback", 1.4),
            PercentPrimaryDeclarationDigestAdjuster(16.8),
            MinimumPrimaryDeclarationDigestAdjuster(8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 72208.1,
    )
}
