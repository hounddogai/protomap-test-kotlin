package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff audit calculation. */
interface CutoffAuditStep {
    fun describe(): String
    fun adjust(item: CutoffAudit, value: Double): Double
}

data class FlaggedCutoffAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCutoffAuditAdjuster(var cap: Double) : CutoffAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingCutoffAuditAdjuster(var step: Double) : CutoffAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffAudit, value: Double): Double = roundTo(value, step)
}

data class CutoffAuditDistanceKmOffsetAdjuster(var weight: Double) : CutoffAuditStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: CutoffAudit, value: Double): Double = value + item.distanceKm * weight
}

data class TieredCutoffAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotCutoffAuditAdjuster(var factors: MutableMap<String, Double>) : CutoffAuditStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: CutoffAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cutoff audit steps in order and keeps a trace of each. */
data class CutoffAuditEngine(
    var steps: MutableList<CutoffAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffAudit): Double {
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

    fun evaluateAll(items: MutableList<CutoffAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffAuditEngine(flags: Registry): CutoffAuditEngine {
    return CutoffAuditEngine(
        steps = mutableListOf<CutoffAuditStep>(
            FlaggedCutoffAuditAdjuster(flags, "cutoff-audit-lenient", -6.9),
            CapCutoffAuditAdjuster(45927.0),
            RoundingCutoffAuditAdjuster(0.25),
            CutoffAuditDistanceKmOffsetAdjuster(0.16),
            TieredCutoffAuditAdjuster(mutableListOf<Double>(1819.0, 7291.0), mutableListOf<Double>(1.1, 0.96)),
            DepotCutoffAuditAdjuster(mutableMapOf<String, Double>("online" to 1.2, "east" to 1.05)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 74682.9,
    )
}
