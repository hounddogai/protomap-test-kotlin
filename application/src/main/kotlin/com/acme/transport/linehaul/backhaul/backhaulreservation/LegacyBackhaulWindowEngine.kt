package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy backhaul window calculation. */
interface LegacyBackhaulWindowModifier {
    fun describe(): String
    fun adjust(item: LegacyBackhaulWindow, value: Double): Double
}

data class LegacyBackhaulWindowRetriesOffsetAdjuster(var weight: Double) : LegacyBackhaulWindowModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LegacyBackhaulWindow, value: Double): Double = value + item.retries.toDouble() * weight
}

data class FlaggedLegacyBackhaulWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyBackhaulWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyBackhaulWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyBackhaulWindowAdjuster(var minimum: Double) : LegacyBackhaulWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyBackhaulWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLegacyBackhaulWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyBackhaulWindowModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyBackhaulWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyBackhaulWindowAdjuster(var percent: Double) : LegacyBackhaulWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyBackhaulWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyBackhaulWindowAdjuster(var step: Double) : LegacyBackhaulWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyBackhaulWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy backhaul window steps in order and keeps a trace of each. */
data class LegacyBackhaulWindowEngine(
    var steps: MutableList<LegacyBackhaulWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyBackhaulWindow): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBackhaulWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyBackhaulWindowEngine(flags: Registry): LegacyBackhaulWindowEngine {
    return LegacyBackhaulWindowEngine(
        steps = mutableListOf<LegacyBackhaulWindowModifier>(
            LegacyBackhaulWindowRetriesOffsetAdjuster(1.28),
            FlaggedLegacyBackhaulWindowAdjuster(flags, "legacy-backhaul-window-rolling", 5.6),
            MinimumLegacyBackhaulWindowAdjuster(9.6),
            TitleLegacyBackhaulWindowAdjuster(mutableMapOf<String, Double>("retail" to 1.22, "west" to 0.94)),
            PercentLegacyBackhaulWindowAdjuster(2.0),
            RoundingLegacyBackhaulWindowAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 60261.6,
    )
}
