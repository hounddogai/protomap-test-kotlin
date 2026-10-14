package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lease return group calculation. */
interface LeaseReturnGroupStep {
    fun describe(): String
    fun adjust(item: LeaseReturnGroup, value: Double): Double
}

data class RegionLeaseReturnGroupAdjuster(var factors: MutableMap<String, Double>) : LeaseReturnGroupStep {
    override fun describe(): String = "by region"

    override fun adjust(item: LeaseReturnGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLeaseReturnGroupAdjuster(var cap: Double) : LeaseReturnGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnGroup, value: Double): Double = minOf(value, cap)
}

data class FlaggedLeaseReturnGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lease return group steps in order and keeps a trace of each. */
data class LeaseReturnGroupEngine(
    var steps: MutableList<LeaseReturnGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnGroup): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnGroupEngine(flags: Registry): LeaseReturnGroupEngine {
    return LeaseReturnGroupEngine(
        steps = mutableListOf<LeaseReturnGroupStep>(
            RegionLeaseReturnGroupAdjuster(
                mutableMapOf<String, Double>("east" to 1.06, "retail" to 0.71, "online" to 0.84),
            ),
            CapLeaseReturnGroupAdjuster(30455.0),
            FlaggedLeaseReturnGroupAdjuster(flags, "lease-return-group-manual", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 10909.0,
    )
}
