package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry

/** One step of the primary tender rollup calculation. */
interface PrimaryTenderRollupModifier {
    fun describe(): String
    fun adjust(item: PrimaryTenderRollup, value: Double): Double
}

data class FlaggedPrimaryTenderRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTenderRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTenderRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentPrimaryTenderRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTenderRollupModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: PrimaryTenderRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryTenderRollupAdjuster(var percent: Double) : PrimaryTenderRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTenderRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryTenderRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTenderRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTenderRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary tender rollup steps in order and keeps a trace of each. */
data class PrimaryTenderRollupEngine(
    var steps: MutableList<PrimaryTenderRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryTenderRollup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTenderRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTenderRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryTenderRollupEngine(flags: Registry): PrimaryTenderRollupEngine {
    return PrimaryTenderRollupEngine(
        steps = mutableListOf<PrimaryTenderRollupModifier>(
            FlaggedPrimaryTenderRollupAdjuster(flags, "primary-tender-rollup-weighted", 9.7),
            SegmentPrimaryTenderRollupAdjuster(
                mutableMapOf<String, Double>("central" to 0.82, "west" to 1.3, "retail" to 1.18),
            ),
            PercentPrimaryTenderRollupAdjuster(-11.8),
            TieredPrimaryTenderRollupAdjuster(
                mutableListOf<Double>(3635.0, 4853.0, 4924.0, 7700.0),
                mutableListOf<Double>(0.92, 1.11, 0.94, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 99649.9,
    )
}
