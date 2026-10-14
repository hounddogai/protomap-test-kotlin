package com.acme.transport.network.zone.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global zone claim calculation. */
interface GlobalZoneClaimModifier {
    fun describe(): String
    fun adjust(item: GlobalZoneClaim, value: Double): Double
}

data class PercentGlobalZoneClaimAdjuster(var percent: Double) : GlobalZoneClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalZoneClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalZoneClaimAdjuster(var cap: Double) : GlobalZoneClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalZoneClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalZoneClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalZoneClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalZoneClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalZoneClaimSequenceOffsetAdjuster(var weight: Double) : GlobalZoneClaimModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: GlobalZoneClaim, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class SourceGlobalZoneClaimAdjuster(var factors: MutableMap<String, Double>) : GlobalZoneClaimModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: GlobalZoneClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalZoneClaimAdjuster(var step: Double) : GlobalZoneClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalZoneClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the global zone claim steps in order and keeps a trace of each. */
data class GlobalZoneClaimEngine(
    var steps: MutableList<GlobalZoneClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalZoneClaim): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalZoneClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalZoneClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalZoneClaimEngine(flags: Registry): GlobalZoneClaimEngine {
    return GlobalZoneClaimEngine(
        steps = mutableListOf<GlobalZoneClaimModifier>(
            PercentGlobalZoneClaimAdjuster(22.7),
            CapGlobalZoneClaimAdjuster(14580.0),
            FlaggedGlobalZoneClaimAdjuster(flags, "global-zone-claim-capped", 7.3),
            GlobalZoneClaimSequenceOffsetAdjuster(1.48),
            SourceGlobalZoneClaimAdjuster(mutableMapOf<String, Double>("south" to 0.71)),
            RoundingGlobalZoneClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 21254.9,
    )
}
