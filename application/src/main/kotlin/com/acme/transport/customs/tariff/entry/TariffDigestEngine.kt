package com.acme.transport.customs.tariff.entry

/** One step of the tariff digest calculation. */
interface TariffDigestModifier {
    fun describe(): String
    fun adjust(item: TariffDigest, value: Double): Double
}

data class TitleTariffDigestAdjuster(var factors: MutableMap<String, Double>) : TariffDigestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TariffDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTariffDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTariffDigestAdjuster(var cap: Double) : TariffDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffDigest, value: Double): Double = minOf(value, cap)
}

data class TariffDigestRatioOffsetAdjuster(var weight: Double) : TariffDigestModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: TariffDigest, value: Double): Double = value + item.ratio * weight
}

/** Runs the tariff digest steps in order and keeps a trace of each. */
data class TariffDigestEngine(
    var steps: MutableList<TariffDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TariffDigest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTariffDigestEngine(): TariffDigestEngine {
    return TariffDigestEngine(
        steps = mutableListOf<TariffDigestModifier>(
            TitleTariffDigestAdjuster(
                mutableMapOf<String, Double>("north" to 0.82, "central" to 1.06, "online" to 1.36),
            ),
            TieredTariffDigestAdjuster(mutableListOf<Double>(6409.0, 9422.0), mutableListOf<Double>(0.97, 0.82)),
            CapTariffDigestAdjuster(47678.0),
            TariffDigestRatioOffsetAdjuster(1.48),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 86962.3,
    )
}
