package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling fuel surcharge ledger calculation. */
interface RollingFuelSurchargeLedgerFactor {
    fun describe(): String
    fun adjust(item: RollingFuelSurchargeLedger, value: Double): Double
}

data class RoundingRollingFuelSurchargeLedgerAdjuster(var step: Double) : RollingFuelSurchargeLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFuelSurchargeLedger, value: Double): Double = roundTo(value, step)
}

data class RollingFuelSurchargeLedgerQuantityOffsetAdjuster(
    var weight: Double,
) : RollingFuelSurchargeLedgerFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RollingFuelSurchargeLedger, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class FlaggedRollingFuelSurchargeLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFuelSurchargeLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFuelSurchargeLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingFuelSurchargeLedgerAdjuster(var cap: Double) : RollingFuelSurchargeLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelSurchargeLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling fuel surcharge ledger steps in order and keeps a trace of each. */
data class RollingFuelSurchargeLedgerEngine(
    var steps: MutableList<RollingFuelSurchargeLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingFuelSurchargeLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingFuelSurchargeLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingFuelSurchargeLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingFuelSurchargeLedgerEngine(flags: Registry): RollingFuelSurchargeLedgerEngine {
    return RollingFuelSurchargeLedgerEngine(
        steps = mutableListOf<RollingFuelSurchargeLedgerFactor>(
            RoundingRollingFuelSurchargeLedgerAdjuster(5.0),
            RollingFuelSurchargeLedgerQuantityOffsetAdjuster(0.87),
            FlaggedRollingFuelSurchargeLedgerAdjuster(flags, "rolling-fuel-surcharge-ledger-legacy", 7.1),
            CapRollingFuelSurchargeLedgerAdjuster(48720.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 30192.7,
    )
}
