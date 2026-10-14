package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive defect audit calculation. */
interface AdaptiveDefectAuditFactor {
    fun describe(): String
    fun adjust(item: AdaptiveDefectAudit, value: Double): Double
}

data class FlaggedAdaptiveDefectAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDefectAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDefectAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelAdaptiveDefectAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDefectAuditFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveDefectAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveDefectAuditUpdatedAtOffsetAdjuster(var weight: Double) : AdaptiveDefectAuditFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: AdaptiveDefectAudit, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class PercentAdaptiveDefectAuditAdjuster(var percent: Double) : AdaptiveDefectAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDefectAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveDefectAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDefectAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDefectAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive defect audit steps in order and keeps a trace of each. */
data class AdaptiveDefectAuditEngine(
    var steps: MutableList<AdaptiveDefectAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveDefectAudit): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDefectAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveDefectAuditEngine(flags: Registry): AdaptiveDefectAuditEngine {
    return AdaptiveDefectAuditEngine(
        steps = mutableListOf<AdaptiveDefectAuditFactor>(
            FlaggedAdaptiveDefectAuditAdjuster(flags, "adaptive-defect-audit-fallback", -7.9),
            LabelAdaptiveDefectAuditAdjuster(mutableMapOf<String, Double>("central" to 1.04, "east" to 0.91)),
            AdaptiveDefectAuditUpdatedAtOffsetAdjuster(1.17),
            PercentAdaptiveDefectAuditAdjuster(3.1),
            TieredAdaptiveDefectAuditAdjuster(
                mutableListOf<Double>(1531.0, 7319.0),
                mutableListOf<Double>(1.2, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 60317.1,
    )
}
