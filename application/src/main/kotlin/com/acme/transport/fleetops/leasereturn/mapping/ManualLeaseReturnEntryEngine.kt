package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual lease return entry calculation. */
interface ManualLeaseReturnEntryModifier {
    fun describe(): String
    fun adjust(item: ManualLeaseReturnEntry, value: Double): Double
}

data class FlaggedManualLeaseReturnEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualLeaseReturnEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualLeaseReturnEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualLeaseReturnEntryAmountOffsetAdjuster(var weight: Double) : ManualLeaseReturnEntryModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ManualLeaseReturnEntry, value: Double): Double = value + item.amount * weight
}

data class CapManualLeaseReturnEntryAdjuster(var cap: Double) : ManualLeaseReturnEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualLeaseReturnEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the manual lease return entry steps in order and keeps a trace of each. */
data class ManualLeaseReturnEntryEngine(
    var steps: MutableList<ManualLeaseReturnEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualLeaseReturnEntry): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualLeaseReturnEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualLeaseReturnEntryEngine(flags: Registry): ManualLeaseReturnEntryEngine {
    return ManualLeaseReturnEntryEngine(
        steps = mutableListOf<ManualLeaseReturnEntryModifier>(
            FlaggedManualLeaseReturnEntryAdjuster(flags, "manual-lease-return-entry-manual", 1.6),
            ManualLeaseReturnEntryAmountOffsetAdjuster(1.41),
            CapManualLeaseReturnEntryAdjuster(22889.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 5019.3,
    )
}
