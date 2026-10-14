package com.acme.transport.network.hub.agreement

/** One step of the rolling hub ledger calculation. */
interface RollingHubLedgerModifier {
    fun describe(): String
    fun adjust(item: RollingHubLedger, value: Double): Double
}

data class RollingHubLedgerCapacityOffsetAdjuster(var weight: Double) : RollingHubLedgerModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RollingHubLedger, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumRollingHubLedgerAdjuster(var minimum: Double) : RollingHubLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingHubLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingHubLedgerAdjuster(var cap: Double) : RollingHubLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingHubLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling hub ledger steps in order and keeps a trace of each. */
data class RollingHubLedgerEngine(
    var steps: MutableList<RollingHubLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingHubLedger): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingHubLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingHubLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingHubLedgerEngine(): RollingHubLedgerEngine {
    return RollingHubLedgerEngine(
        steps = mutableListOf<RollingHubLedgerModifier>(
            RollingHubLedgerCapacityOffsetAdjuster(0.52),
            MinimumRollingHubLedgerAdjuster(33.9),
            CapRollingHubLedgerAdjuster(8297.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 20949.5,
    )
}
