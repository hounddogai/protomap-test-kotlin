package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling idling ledger calculation. */
interface RollingIdlingLedgerModifier {
    fun describe(): String
    fun adjust(item: RollingIdlingLedger, value: Double): Double
}

data class PercentRollingIdlingLedgerAdjuster(var percent: Double) : RollingIdlingLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingIdlingLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingIdlingLedgerAdjuster(var minimum: Double) : RollingIdlingLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingIdlingLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceRollingIdlingLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingIdlingLedgerModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: RollingIdlingLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingIdlingLedgerAdjuster(var cap: Double) : RollingIdlingLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingIdlingLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling idling ledger steps in order and keeps a trace of each. */
data class RollingIdlingLedgerEngine(
    var steps: MutableList<RollingIdlingLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingIdlingLedger): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingIdlingLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingIdlingLedgerEngine(): RollingIdlingLedgerEngine {
    return RollingIdlingLedgerEngine(
        steps = mutableListOf<RollingIdlingLedgerModifier>(
            PercentRollingIdlingLedgerAdjuster(23.5),
            MinimumRollingIdlingLedgerAdjuster(38.4),
            SourceRollingIdlingLedgerAdjuster(
                mutableMapOf<String, Double>("south" to 1.32, "north" to 1.11, "retail" to 1.13),
            ),
            CapRollingIdlingLedgerAdjuster(33092.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 96978.1,
    )
}
