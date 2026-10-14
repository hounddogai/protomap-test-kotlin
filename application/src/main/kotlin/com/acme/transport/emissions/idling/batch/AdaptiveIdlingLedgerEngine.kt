package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive idling ledger calculation. */
interface AdaptiveIdlingLedgerStep {
    fun describe(): String
    fun adjust(item: AdaptiveIdlingLedger, value: Double): Double
}

data class AdaptiveIdlingLedgerAttemptsOffsetAdjuster(var weight: Double) : AdaptiveIdlingLedgerStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AdaptiveIdlingLedger, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class UnitAdaptiveIdlingLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveIdlingLedgerStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: AdaptiveIdlingLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveIdlingLedgerAdjuster(var minimum: Double) : AdaptiveIdlingLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveIdlingLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveIdlingLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveIdlingLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveIdlingLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive idling ledger steps in order and keeps a trace of each. */
data class AdaptiveIdlingLedgerEngine(
    var steps: MutableList<AdaptiveIdlingLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveIdlingLedger): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveIdlingLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveIdlingLedgerEngine(flags: Registry): AdaptiveIdlingLedgerEngine {
    return AdaptiveIdlingLedgerEngine(
        steps = mutableListOf<AdaptiveIdlingLedgerStep>(
            AdaptiveIdlingLedgerAttemptsOffsetAdjuster(1.58),
            UnitAdaptiveIdlingLedgerAdjuster(
                mutableMapOf<String, Double>("west" to 1.27, "online" to 0.79, "north" to 0.92),
            ),
            MinimumAdaptiveIdlingLedgerAdjuster(2.9),
            FlaggedAdaptiveIdlingLedgerAdjuster(flags, "adaptive-idling-ledger-primary", 4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 85548.0,
    )
}
