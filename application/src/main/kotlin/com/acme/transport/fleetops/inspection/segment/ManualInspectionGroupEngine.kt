package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual inspection group calculation. */
interface ManualInspectionGroupAdjuster {
    fun describe(): String
    fun adjust(item: ManualInspectionGroup, value: Double): Double
}

data class ManualInspectionGroupLoadKgOffsetAdjuster(var weight: Double) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double = value + item.loadKg * weight
}

data class MinimumManualInspectionGroupAdjuster(var minimum: Double) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualInspectionGroupAdjuster(var percent: Double) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualInspectionGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameManualInspectionGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual inspection group steps in order and keeps a trace of each. */
data class ManualInspectionGroupEngine(
    var steps: MutableList<ManualInspectionGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualInspectionGroup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualInspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualInspectionGroupEngine(flags: Registry): ManualInspectionGroupEngine {
    return ManualInspectionGroupEngine(
        steps = mutableListOf<ManualInspectionGroupAdjuster>(
            ManualInspectionGroupLoadKgOffsetAdjuster(0.98),
            MinimumManualInspectionGroupAdjuster(8.2),
            PercentManualInspectionGroupAdjuster(22.7),
            FlaggedManualInspectionGroupAdjuster(flags, "manual-inspection-group-regional", 5.2),
            NameManualInspectionGroupAdjuster(
                mutableMapOf<String, Double>("south" to 0.91, "west" to 0.97, "online" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 21149.9,
    )
}
