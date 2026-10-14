package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy defect snapshot calculation. */
interface LegacyDefectSnapshotModifier {
    fun describe(): String
    fun adjust(item: LegacyDefectSnapshot, value: Double): Double
}

data class LegacyDefectSnapshotRatioOffsetAdjuster(var weight: Double) : LegacyDefectSnapshotModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: LegacyDefectSnapshot, value: Double): Double = value + item.ratio * weight
}

data class RoundingLegacyDefectSnapshotAdjuster(var step: Double) : LegacyDefectSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDefectSnapshot, value: Double): Double = roundTo(value, step)
}

data class NameLegacyDefectSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDefectSnapshotModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyDefectSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyDefectSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDefectSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDefectSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyDefectSnapshotAdjuster(var cap: Double) : LegacyDefectSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDefectSnapshot, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyDefectSnapshotAdjuster(var percent: Double) : LegacyDefectSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDefectSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy defect snapshot steps in order and keeps a trace of each. */
data class LegacyDefectSnapshotEngine(
    var steps: MutableList<LegacyDefectSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyDefectSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyDefectSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyDefectSnapshotEngine(flags: Registry): LegacyDefectSnapshotEngine {
    return LegacyDefectSnapshotEngine(
        steps = mutableListOf<LegacyDefectSnapshotModifier>(
            LegacyDefectSnapshotRatioOffsetAdjuster(1.03),
            RoundingLegacyDefectSnapshotAdjuster(0.5),
            NameLegacyDefectSnapshotAdjuster(
                mutableMapOf<String, Double>("retail" to 0.92, "south" to 1.26, "online" to 0.88),
            ),
            FlaggedLegacyDefectSnapshotAdjuster(flags, "legacy-defect-snapshot-capped", -3.9),
            CapLegacyDefectSnapshotAdjuster(38996.0),
            PercentLegacyDefectSnapshotAdjuster(-8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 20724.6,
    )
}
