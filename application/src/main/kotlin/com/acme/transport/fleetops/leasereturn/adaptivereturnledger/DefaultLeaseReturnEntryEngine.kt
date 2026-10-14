package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default lease return entry calculation. */
interface DefaultLeaseReturnEntryModifier {
    fun describe(): String
    fun adjust(item: DefaultLeaseReturnEntry, value: Double): Double
}

data class NameDefaultLeaseReturnEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultLeaseReturnEntryModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultLeaseReturnEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultLeaseReturnEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultLeaseReturnEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultLeaseReturnEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefaultLeaseReturnEntryAdjuster(var minimum: Double) : DefaultLeaseReturnEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultLeaseReturnEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default lease return entry steps in order and keeps a trace of each. */
data class DefaultLeaseReturnEntryEngine(
    var steps: MutableList<DefaultLeaseReturnEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultLeaseReturnEntry): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultLeaseReturnEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultLeaseReturnEntryEngine(flags: Registry): DefaultLeaseReturnEntryEngine {
    return DefaultLeaseReturnEntryEngine(
        steps = mutableListOf<DefaultLeaseReturnEntryModifier>(
            NameDefaultLeaseReturnEntryAdjuster(mutableMapOf<String, Double>("south" to 1.36)),
            FlaggedDefaultLeaseReturnEntryAdjuster(flags, "default-lease-return-entry-manual", 6.5),
            MinimumDefaultLeaseReturnEntryAdjuster(13.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 72744.7,
    )
}
