package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling lease return ledger calculation. */
interface RollingLeaseReturnLedgerModifier {
    fun describe(): String
    fun adjust(item: RollingLeaseReturnLedger, value: Double): Double
}

data class MinimumRollingLeaseReturnLedgerAdjuster(var minimum: Double) : RollingLeaseReturnLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingLeaseReturnLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingLeaseReturnLedgerLoadKgOffsetAdjuster(var weight: Double) : RollingLeaseReturnLedgerModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RollingLeaseReturnLedger, value: Double): Double = value + item.loadKg * weight
}

data class RoundingRollingLeaseReturnLedgerAdjuster(var step: Double) : RollingLeaseReturnLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingLeaseReturnLedger, value: Double): Double = roundTo(value, step)
}

data class PercentRollingLeaseReturnLedgerAdjuster(var percent: Double) : RollingLeaseReturnLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingLeaseReturnLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelRollingLeaseReturnLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingLeaseReturnLedgerModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingLeaseReturnLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling lease return ledger steps in order and keeps a trace of each. */
data class RollingLeaseReturnLedgerEngine(
    var steps: MutableList<RollingLeaseReturnLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingLeaseReturnLedger): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingLeaseReturnLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingLeaseReturnLedgerEngine(): RollingLeaseReturnLedgerEngine {
    return RollingLeaseReturnLedgerEngine(
        steps = mutableListOf<RollingLeaseReturnLedgerModifier>(
            MinimumRollingLeaseReturnLedgerAdjuster(33.2),
            RollingLeaseReturnLedgerLoadKgOffsetAdjuster(0.27),
            RoundingRollingLeaseReturnLedgerAdjuster(0.1),
            PercentRollingLeaseReturnLedgerAdjuster(-14.2),
            LabelRollingLeaseReturnLedgerAdjuster(
                mutableMapOf<String, Double>("south" to 1.3, "online" to 1.21, "central" to 0.71),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 91637.4,
    )
}
