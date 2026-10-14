package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff audit calculation. */
interface TariffAuditStep {
    fun describe(): String
    fun adjust(item: TariffAudit, value: Double): Double
}

data class RoundingTariffAuditAdjuster(var step: Double) : TariffAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedTariffAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTariffAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceTariffAuditAdjuster(var factors: MutableMap<String, Double>) : TariffAuditStep {
    override fun describe(): String = "by source"

    override fun adjust(item: TariffAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TariffAuditDiscountOffsetAdjuster(var weight: Double) : TariffAuditStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: TariffAudit, value: Double): Double = value + item.discount * weight
}

data class CapTariffAuditAdjuster(var cap: Double) : TariffAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the tariff audit steps in order and keeps a trace of each. */
data class TariffAuditEngine(
    var steps: MutableList<TariffAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TariffAudit): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTariffAuditEngine(flags: Registry): TariffAuditEngine {
    return TariffAuditEngine(
        steps = mutableListOf<TariffAuditStep>(
            RoundingTariffAuditAdjuster(0.25),
            FlaggedTariffAuditAdjuster(flags, "tariff-audit-pending", 2.2),
            TieredTariffAuditAdjuster(mutableListOf<Double>(1769.0, 9792.0), mutableListOf<Double>(0.95, 1.14)),
            SourceTariffAuditAdjuster(
                mutableMapOf<String, Double>("west" to 1.11, "online" to 0.81, "south" to 1.29),
            ),
            TariffAuditDiscountOffsetAdjuster(0.3),
            CapTariffAuditAdjuster(43724.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 9213.0,
    )
}
