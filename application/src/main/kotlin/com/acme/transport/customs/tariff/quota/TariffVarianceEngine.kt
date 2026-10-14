package com.acme.transport.customs.tariff.quota

/** One step of the tariff variance calculation. */
interface TariffVarianceModifier {
    fun describe(): String
    fun adjust(item: TariffVariance, value: Double): Double
}

data class PercentTariffVarianceAdjuster(var percent: Double) : TariffVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TariffVarianceDiscountOffsetAdjuster(var weight: Double) : TariffVarianceModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: TariffVariance, value: Double): Double = value + item.discount * weight
}

data class SegmentTariffVarianceAdjuster(var factors: MutableMap<String, Double>) : TariffVarianceModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: TariffVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tariff variance steps in order and keeps a trace of each. */
data class TariffVarianceEngine(
    var steps: MutableList<TariffVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffVariance): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffVarianceEngine(): TariffVarianceEngine {
    return TariffVarianceEngine(
        steps = mutableListOf<TariffVarianceModifier>(
            PercentTariffVarianceAdjuster(-24.3),
            TariffVarianceDiscountOffsetAdjuster(1.41),
            SegmentTariffVarianceAdjuster(mutableMapOf<String, Double>("north" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 86374.6,
    )
}
