package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.flags.Registry

/** One step of the route offset ledger calculation. */
interface RouteOffsetLedgerStep {
    fun describe(): String
    fun adjust(item: RouteOffsetLedger, value: Double): Double
}

data class PercentRouteOffsetLedgerAdjuster(var percent: Double) : RouteOffsetLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRouteOffsetLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRouteOffsetLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the route offset ledger steps in order and keeps a trace of each. */
data class RouteOffsetLedgerEngine(
    var steps: MutableList<RouteOffsetLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RouteOffsetLedger): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRouteOffsetLedgerEngine(flags: Registry): RouteOffsetLedgerEngine {
    return RouteOffsetLedgerEngine(
        steps = mutableListOf<RouteOffsetLedgerStep>(
            PercentRouteOffsetLedgerAdjuster(-21.3),
            TieredRouteOffsetLedgerAdjuster(
                mutableListOf<Double>(52.0, 3496.0, 5572.0),
                mutableListOf<Double>(0.81, 0.86, 1.12),
            ),
            FlaggedRouteOffsetLedgerAdjuster(flags, "route-offset-ledger-legacy", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 75941.8,
    )
}
