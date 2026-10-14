package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending sortation digest calculation. */
interface PendingSortationDigestModifier {
    fun describe(): String
    fun adjust(item: PendingSortationDigest, value: Double): Double
}

data class FlaggedPendingSortationDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingSortationDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingSortationDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingSortationDigestAdjuster(var minimum: Double) : PendingSortationDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingSortationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitPendingSortationDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingSortationDigestModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingSortationDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending sortation digest steps in order and keeps a trace of each. */
data class PendingSortationDigestEngine(
    var steps: MutableList<PendingSortationDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingSortationDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingSortationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingSortationDigestEngine(flags: Registry): PendingSortationDigestEngine {
    return PendingSortationDigestEngine(
        steps = mutableListOf<PendingSortationDigestModifier>(
            FlaggedPendingSortationDigestAdjuster(flags, "pending-sortation-digest-weighted", -1.7),
            MinimumPendingSortationDigestAdjuster(4.6),
            UnitPendingSortationDigestAdjuster(mutableMapOf<String, Double>("south" to 0.91, "north" to 1.35)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 66714.3,
    )
}
