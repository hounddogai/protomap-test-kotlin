package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire ledger calculation. */
interface TireLedgerStep {
    fun describe(): String
    fun adjust(item: TireLedger, value: Double): Double
}

data class TieredTireLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TireLedgerThresholdOffsetAdjuster(var weight: Double) : TireLedgerStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: TireLedger, value: Double): Double = value + item.threshold * weight
}

data class UnitTireLedgerAdjuster(var factors: MutableMap<String, Double>) : TireLedgerStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: TireLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTireLedgerAdjuster(var minimum: Double) : TireLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTireLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTireLedgerAdjuster(var percent: Double) : TireLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tire ledger steps in order and keeps a trace of each. */
data class TireLedgerEngine(
    var steps: MutableList<TireLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireLedger): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireLedgerEngine(flags: Registry): TireLedgerEngine {
    return TireLedgerEngine(
        steps = mutableListOf<TireLedgerStep>(
            TieredTireLedgerAdjuster(
                mutableListOf<Double>(4713.0, 5331.0, 7311.0),
                mutableListOf<Double>(0.94, 0.91, 1.16),
            ),
            TireLedgerThresholdOffsetAdjuster(1.33),
            UnitTireLedgerAdjuster(mutableMapOf<String, Double>("west" to 1.1, "retail" to 1.0)),
            MinimumTireLedgerAdjuster(33.6),
            FlaggedTireLedgerAdjuster(flags, "tire-ledger-legacy", 0.9),
            PercentTireLedgerAdjuster(-8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 78277.6,
    )
}
