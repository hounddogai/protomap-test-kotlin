package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy inspection rule calculation. */
interface LegacyInspectionRuleAdjuster {
    fun describe(): String
    fun adjust(item: LegacyInspectionRule, value: Double): Double
}

data class PercentLegacyInspectionRuleAdjuster(var percent: Double) : LegacyInspectionRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyInspectionRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyInspectionRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyInspectionRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyInspectionRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyInspectionRuleAdjuster(var minimum: Double) : LegacyInspectionRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyInspectionRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameLegacyInspectionRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionRuleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyInspectionRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyInspectionRuleAdjuster(var cap: Double) : LegacyInspectionRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyInspectionRule, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyInspectionRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyInspectionRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyInspectionRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy inspection rule steps in order and keeps a trace of each. */
data class LegacyInspectionRuleEngine(
    var steps: MutableList<LegacyInspectionRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyInspectionRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyInspectionRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyInspectionRuleEngine(flags: Registry): LegacyInspectionRuleEngine {
    return LegacyInspectionRuleEngine(
        steps = mutableListOf<LegacyInspectionRuleAdjuster>(
            PercentLegacyInspectionRuleAdjuster(2.3),
            FlaggedLegacyInspectionRuleAdjuster(flags, "legacy-inspection-rule-batched", 6.1),
            MinimumLegacyInspectionRuleAdjuster(34.4),
            NameLegacyInspectionRuleAdjuster(mutableMapOf<String, Double>("north" to 1.04)),
            CapLegacyInspectionRuleAdjuster(38963.0),
            TieredLegacyInspectionRuleAdjuster(
                mutableListOf<Double>(3360.0, 3670.0, 5687.0, 8030.0),
                mutableListOf<Double>(1.11, 1.18, 1.06, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 43654.4,
    )
}
