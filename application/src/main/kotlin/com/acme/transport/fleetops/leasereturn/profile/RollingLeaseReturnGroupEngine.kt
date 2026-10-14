package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling lease return group calculation. */
interface RollingLeaseReturnGroupAdjuster {
    fun describe(): String
    fun adjust(item: RollingLeaseReturnGroup, value: Double): Double
}

data class MinimumRollingLeaseReturnGroupAdjuster(var minimum: Double) : RollingLeaseReturnGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingLeaseReturnGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingLeaseReturnGroupAdjuster(var percent: Double) : RollingLeaseReturnGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingLeaseReturnGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingLeaseReturnGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingLeaseReturnGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingLeaseReturnGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRollingLeaseReturnGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingLeaseReturnGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingLeaseReturnGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling lease return group steps in order and keeps a trace of each. */
data class RollingLeaseReturnGroupEngine(
    var steps: MutableList<RollingLeaseReturnGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingLeaseReturnGroup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingLeaseReturnGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingLeaseReturnGroupEngine(flags: Registry): RollingLeaseReturnGroupEngine {
    return RollingLeaseReturnGroupEngine(
        steps = mutableListOf<RollingLeaseReturnGroupAdjuster>(
            MinimumRollingLeaseReturnGroupAdjuster(29.3),
            PercentRollingLeaseReturnGroupAdjuster(6.1),
            FlaggedRollingLeaseReturnGroupAdjuster(flags, "rolling-lease-return-group-pending", -0.7),
            NameRollingLeaseReturnGroupAdjuster(mutableMapOf<String, Double>("west" to 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 66947.8,
    )
}
