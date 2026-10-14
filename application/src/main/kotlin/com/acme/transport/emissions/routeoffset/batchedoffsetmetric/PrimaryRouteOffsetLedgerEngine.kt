package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary route offset ledger calculation. */
interface PrimaryRouteOffsetLedgerFactor {
    fun describe(): String
    fun adjust(item: PrimaryRouteOffsetLedger, value: Double): Double
}

data class LanePrimaryRouteOffsetLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRouteOffsetLedgerFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: PrimaryRouteOffsetLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryRouteOffsetLedgerThresholdOffsetAdjuster(
    var weight: Double,
) : PrimaryRouteOffsetLedgerFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PrimaryRouteOffsetLedger, value: Double): Double = value + item.threshold * weight
}

data class FlaggedPrimaryRouteOffsetLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryRouteOffsetLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryRouteOffsetLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryRouteOffsetLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRouteOffsetLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRouteOffsetLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary route offset ledger steps in order and keeps a trace of each. */
data class PrimaryRouteOffsetLedgerEngine(
    var steps: MutableList<PrimaryRouteOffsetLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryRouteOffsetLedger): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryRouteOffsetLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryRouteOffsetLedgerEngine(flags: Registry): PrimaryRouteOffsetLedgerEngine {
    return PrimaryRouteOffsetLedgerEngine(
        steps = mutableListOf<PrimaryRouteOffsetLedgerFactor>(
            LanePrimaryRouteOffsetLedgerAdjuster(
                mutableMapOf<String, Double>("north" to 0.9, "central" to 0.87, "retail" to 1.14),
            ),
            PrimaryRouteOffsetLedgerThresholdOffsetAdjuster(1.63),
            FlaggedPrimaryRouteOffsetLedgerAdjuster(flags, "primary-route-offset-ledger-legacy", 9.6),
            TieredPrimaryRouteOffsetLedgerAdjuster(
                mutableListOf<Double>(886.0, 7866.0),
                mutableListOf<Double>(0.87, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 28368.6,
    )
}
