package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global redelivery ledger calculation. */
interface GlobalRedeliveryLedgerFactor {
    fun describe(): String
    fun adjust(item: GlobalRedeliveryLedger, value: Double): Double
}

data class RoundingGlobalRedeliveryLedgerAdjuster(var step: Double) : GlobalRedeliveryLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRedeliveryLedger, value: Double): Double = roundTo(value, step)
}

data class DepotGlobalRedeliveryLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRedeliveryLedgerFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalRedeliveryLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalRedeliveryLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRedeliveryLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRedeliveryLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalRedeliveryLedgerAdjuster(var cap: Double) : GlobalRedeliveryLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRedeliveryLedger, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalRedeliveryLedgerAdjuster(var percent: Double) : GlobalRedeliveryLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRedeliveryLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalRedeliveryLedgerThresholdOffsetAdjuster(var weight: Double) : GlobalRedeliveryLedgerFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: GlobalRedeliveryLedger, value: Double): Double = value + item.threshold * weight
}

/** Runs the global redelivery ledger steps in order and keeps a trace of each. */
data class GlobalRedeliveryLedgerEngine(
    var steps: MutableList<GlobalRedeliveryLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRedeliveryLedger): Double {
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

    fun evaluateAll(items: MutableList<GlobalRedeliveryLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRedeliveryLedgerEngine(flags: Registry): GlobalRedeliveryLedgerEngine {
    return GlobalRedeliveryLedgerEngine(
        steps = mutableListOf<GlobalRedeliveryLedgerFactor>(
            RoundingGlobalRedeliveryLedgerAdjuster(5.0),
            DepotGlobalRedeliveryLedgerAdjuster(
                mutableMapOf<String, Double>("east" to 0.74, "west" to 1.38, "central" to 1.16),
            ),
            FlaggedGlobalRedeliveryLedgerAdjuster(flags, "global-redelivery-ledger-scheduled", -5.4),
            CapGlobalRedeliveryLedgerAdjuster(1734.0),
            PercentGlobalRedeliveryLedgerAdjuster(0.9),
            GlobalRedeliveryLedgerThresholdOffsetAdjuster(1.81),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 24403.4,
    )
}
