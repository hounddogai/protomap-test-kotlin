package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.flags.Registry

/** One step of the locker ledger calculation. */
interface LockerLedgerStep {
    fun describe(): String
    fun adjust(item: LockerLedger, value: Double): Double
}

data class MinimumLockerLedgerAdjuster(var minimum: Double) : LockerLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLockerLedgerAdjuster(var factors: MutableMap<String, Double>) : LockerLedgerStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LockerLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLockerLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the locker ledger steps in order and keeps a trace of each. */
data class LockerLedgerEngine(
    var steps: MutableList<LockerLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerLedger): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerLedgerEngine(flags: Registry): LockerLedgerEngine {
    return LockerLedgerEngine(
        steps = mutableListOf<LockerLedgerStep>(
            MinimumLockerLedgerAdjuster(1.4),
            TitleLockerLedgerAdjuster(
                mutableMapOf<String, Double>("west" to 1.08, "north" to 1.13, "east" to 1.1),
            ),
            FlaggedLockerLedgerAdjuster(flags, "locker-ledger-manual", -6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 84945.1,
    )
}
