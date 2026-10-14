package com.acme.transport.customs.tariff.manifest

/** One step of the tariff manifest calculation. */
interface TariffManifestModifier {
    fun describe(): String
    fun adjust(item: TariffManifest, value: Double): Double
}

data class MinimumTariffManifestAdjuster(var minimum: Double) : TariffManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTariffManifestAdjuster(var percent: Double) : TariffManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryTariffManifestAdjuster(var factors: MutableMap<String, Double>) : TariffManifestModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: TariffManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tariff manifest steps in order and keeps a trace of each. */
data class TariffManifestEngine(
    var steps: MutableList<TariffManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffManifest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffManifestEngine(): TariffManifestEngine {
    return TariffManifestEngine(
        steps = mutableListOf<TariffManifestModifier>(
            MinimumTariffManifestAdjuster(19.8),
            PercentTariffManifestAdjuster(19.6),
            CategoryTariffManifestAdjuster(
                mutableMapOf<String, Double>("east" to 1.25, "north" to 0.82, "online" to 1.31),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 49586.4,
    )
}
