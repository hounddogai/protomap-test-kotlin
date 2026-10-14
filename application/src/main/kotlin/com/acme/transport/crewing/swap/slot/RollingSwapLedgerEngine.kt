package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling swap ledger calculation. */
interface RollingSwapLedgerModifier {
    fun describe(): String
    fun adjust(item: RollingSwapLedger, value: Double): Double
}

data class FlaggedRollingSwapLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingSwapLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingSwapLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingSwapLedgerAdjuster(var step: Double) : RollingSwapLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingSwapLedger, value: Double): Double = roundTo(value, step)
}

data class PercentRollingSwapLedgerAdjuster(var percent: Double) : RollingSwapLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingSwapLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingSwapLedgerAdjuster(var cap: Double) : RollingSwapLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingSwapLedger, value: Double): Double = minOf(value, cap)
}

data class RollingSwapLedgerUpdatedAtOffsetAdjuster(var weight: Double) : RollingSwapLedgerModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RollingSwapLedger, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class MinimumRollingSwapLedgerAdjuster(var minimum: Double) : RollingSwapLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingSwapLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling swap ledger steps in order and keeps a trace of each. */
data class RollingSwapLedgerEngine(
    var steps: MutableList<RollingSwapLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingSwapLedger): Double {
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

    fun evaluateAll(items: MutableList<RollingSwapLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingSwapLedgerEngine(flags: Registry): RollingSwapLedgerEngine {
    return RollingSwapLedgerEngine(
        steps = mutableListOf<RollingSwapLedgerModifier>(
            FlaggedRollingSwapLedgerAdjuster(flags, "rolling-swap-ledger-global", 7.7),
            RoundingRollingSwapLedgerAdjuster(1.0),
            PercentRollingSwapLedgerAdjuster(21.8),
            CapRollingSwapLedgerAdjuster(43814.0),
            RollingSwapLedgerUpdatedAtOffsetAdjuster(1.64),
            MinimumRollingSwapLedgerAdjuster(35.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 88471.3,
    )
}
