package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional capacity assignment calculation. */
interface RegionalCapacityAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: RegionalCapacityAssignment, value: Double): Double
}

data class PercentRegionalCapacityAssignmentAdjuster(var percent: Double) : RegionalCapacityAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCapacityAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRegionalCapacityAssignmentAdjuster(var cap: Double) : RegionalCapacityAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCapacityAssignment, value: Double): Double = minOf(value, cap)
}

data class SourceRegionalCapacityAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCapacityAssignmentAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalCapacityAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalCapacityAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCapacityAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCapacityAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional capacity assignment steps in order and keeps a trace of each. */
data class RegionalCapacityAssignmentEngine(
    var steps: MutableList<RegionalCapacityAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalCapacityAssignment): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCapacityAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalCapacityAssignmentEngine(flags: Registry): RegionalCapacityAssignmentEngine {
    return RegionalCapacityAssignmentEngine(
        steps = mutableListOf<RegionalCapacityAssignmentAdjuster>(
            PercentRegionalCapacityAssignmentAdjuster(-16.4),
            CapRegionalCapacityAssignmentAdjuster(12476.0),
            SourceRegionalCapacityAssignmentAdjuster(
                mutableMapOf<String, Double>("south" to 1.22, "west" to 1.31, "east" to 1.15),
            ),
            FlaggedRegionalCapacityAssignmentAdjuster(flags, "regional-capacity-assignment-fallback", 5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 4858.1,
    )
}
