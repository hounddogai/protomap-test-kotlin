package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy backhaul quota calculation. */
interface LegacyBackhaulQuotaModifier {
    fun describe(): String
    fun adjust(item: LegacyBackhaulQuota, value: Double): Double
}

data class UnitLegacyBackhaulQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyBackhaulQuotaModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: LegacyBackhaulQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyBackhaulQuotaAdjuster(var cap: Double) : LegacyBackhaulQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyBackhaulQuota, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyBackhaulQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyBackhaulQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyBackhaulQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyBackhaulQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyBackhaulQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyBackhaulQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyBackhaulQuotaAdjuster(var percent: Double) : LegacyBackhaulQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyBackhaulQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy backhaul quota steps in order and keeps a trace of each. */
data class LegacyBackhaulQuotaEngine(
    var steps: MutableList<LegacyBackhaulQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyBackhaulQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBackhaulQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyBackhaulQuotaEngine(flags: Registry): LegacyBackhaulQuotaEngine {
    return LegacyBackhaulQuotaEngine(
        steps = mutableListOf<LegacyBackhaulQuotaModifier>(
            UnitLegacyBackhaulQuotaAdjuster(mutableMapOf<String, Double>("online" to 0.95)),
            CapLegacyBackhaulQuotaAdjuster(1555.0),
            FlaggedLegacyBackhaulQuotaAdjuster(flags, "legacy-backhaul-quota-seasonal", 0.1),
            TieredLegacyBackhaulQuotaAdjuster(
                mutableListOf<Double>(4080.0, 6320.0),
                mutableListOf<Double>(0.84, 1.2),
            ),
            PercentLegacyBackhaulQuotaAdjuster(7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 72172.5,
    )
}
