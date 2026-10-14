package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.flags.Registry

/** One step of the legacy tariff allocation calculation. */
interface LegacyTariffAllocationStep {
    fun describe(): String
    fun adjust(item: LegacyTariffAllocation, value: Double): Double
}

data class FlaggedLegacyTariffAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTariffAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTariffAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyTariffAllocationAdjuster(var cap: Double) : LegacyTariffAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTariffAllocation, value: Double): Double = minOf(value, cap)
}

data class UnitLegacyTariffAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTariffAllocationStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: LegacyTariffAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyTariffAllocationAdjuster(var minimum: Double) : LegacyTariffAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTariffAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyTariffAllocationAdjuster(var percent: Double) : LegacyTariffAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTariffAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy tariff allocation steps in order and keeps a trace of each. */
data class LegacyTariffAllocationEngine(
    var steps: MutableList<LegacyTariffAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyTariffAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTariffAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTariffAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyTariffAllocationEngine(flags: Registry): LegacyTariffAllocationEngine {
    return LegacyTariffAllocationEngine(
        steps = mutableListOf<LegacyTariffAllocationStep>(
            FlaggedLegacyTariffAllocationAdjuster(flags, "legacy-tariff-allocation-primary", -0.2),
            CapLegacyTariffAllocationAdjuster(13202.0),
            UnitLegacyTariffAllocationAdjuster(mutableMapOf<String, Double>("north" to 0.83)),
            MinimumLegacyTariffAllocationAdjuster(32.5),
            PercentLegacyTariffAllocationAdjuster(11.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 57099.3,
    )
}
