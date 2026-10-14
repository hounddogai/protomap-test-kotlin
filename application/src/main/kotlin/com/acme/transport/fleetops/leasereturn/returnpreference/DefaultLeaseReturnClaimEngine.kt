package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default lease return claim calculation. */
interface DefaultLeaseReturnClaimModifier {
    fun describe(): String
    fun adjust(item: DefaultLeaseReturnClaim, value: Double): Double
}

data class FlaggedDefaultLeaseReturnClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultLeaseReturnClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultLeaseReturnClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultLeaseReturnClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultLeaseReturnClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultLeaseReturnClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitDefaultLeaseReturnClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultLeaseReturnClaimModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: DefaultLeaseReturnClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultLeaseReturnClaimAdjuster(var percent: Double) : DefaultLeaseReturnClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLeaseReturnClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultLeaseReturnClaimAdjuster(var minimum: Double) : DefaultLeaseReturnClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultLeaseReturnClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default lease return claim steps in order and keeps a trace of each. */
data class DefaultLeaseReturnClaimEngine(
    var steps: MutableList<DefaultLeaseReturnClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultLeaseReturnClaim): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultLeaseReturnClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultLeaseReturnClaimEngine(flags: Registry): DefaultLeaseReturnClaimEngine {
    return DefaultLeaseReturnClaimEngine(
        steps = mutableListOf<DefaultLeaseReturnClaimModifier>(
            FlaggedDefaultLeaseReturnClaimAdjuster(flags, "default-lease-return-claim-legacy", 8.5),
            TieredDefaultLeaseReturnClaimAdjuster(
                mutableListOf<Double>(3007.0, 4971.0),
                mutableListOf<Double>(0.89, 0.95),
            ),
            UnitDefaultLeaseReturnClaimAdjuster(mutableMapOf<String, Double>("north" to 0.94, "south" to 0.8)),
            PercentDefaultLeaseReturnClaimAdjuster(-1.3),
            MinimumDefaultLeaseReturnClaimAdjuster(12.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 12079.2,
    )
}
