package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the default backhaul snapshot calculation. */
interface DefaultBackhaulSnapshotModifier {
    fun describe(): String
    fun adjust(item: DefaultBackhaulSnapshot, value: Double): Double
}

data class UnitDefaultBackhaulSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultBackhaulSnapshotModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: DefaultBackhaulSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultBackhaulSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultBackhaulSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultBackhaulSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultBackhaulSnapshotAdjuster(var cap: Double) : DefaultBackhaulSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultBackhaulSnapshot, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultBackhaulSnapshotAdjuster(var percent: Double) : DefaultBackhaulSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultBackhaulSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default backhaul snapshot steps in order and keeps a trace of each. */
data class DefaultBackhaulSnapshotEngine(
    var steps: MutableList<DefaultBackhaulSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultBackhaulSnapshot): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultBackhaulSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultBackhaulSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultBackhaulSnapshotEngine(flags: Registry): DefaultBackhaulSnapshotEngine {
    return DefaultBackhaulSnapshotEngine(
        steps = mutableListOf<DefaultBackhaulSnapshotModifier>(
            UnitDefaultBackhaulSnapshotAdjuster(mutableMapOf<String, Double>("central" to 0.78)),
            FlaggedDefaultBackhaulSnapshotAdjuster(flags, "default-backhaul-snapshot-rolling", -3.0),
            CapDefaultBackhaulSnapshotAdjuster(11144.0),
            PercentDefaultBackhaulSnapshotAdjuster(-7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 67683.4,
    )
}
