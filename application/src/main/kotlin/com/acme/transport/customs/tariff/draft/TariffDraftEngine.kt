package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.flags.Registry

/** One step of the tariff draft calculation. */
interface TariffDraftFactor {
    fun describe(): String
    fun adjust(item: TariffDraft, value: Double): Double
}

data class TieredTariffDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TariffDraftCeilingOffsetAdjuster(var weight: Double) : TariffDraftFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: TariffDraft, value: Double): Double = value + item.ceiling * weight
}

data class PercentTariffDraftAdjuster(var percent: Double) : TariffDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameTariffDraftAdjuster(var factors: MutableMap<String, Double>) : TariffDraftFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TariffDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTariffDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tariff draft steps in order and keeps a trace of each. */
data class TariffDraftEngine(
    var steps: MutableList<TariffDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffDraft): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TariffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffDraftEngine(flags: Registry): TariffDraftEngine {
    return TariffDraftEngine(
        steps = mutableListOf<TariffDraftFactor>(
            TieredTariffDraftAdjuster(
                mutableListOf<Double>(2249.0, 4757.0, 7489.0),
                mutableListOf<Double>(1.1, 1.02, 0.86),
            ),
            TariffDraftCeilingOffsetAdjuster(0.8),
            PercentTariffDraftAdjuster(1.5),
            NameTariffDraftAdjuster(mutableMapOf<String, Double>("retail" to 0.87, "north" to 0.72)),
            FlaggedTariffDraftAdjuster(flags, "tariff-draft-weighted", 6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 5727.2,
    )
}
