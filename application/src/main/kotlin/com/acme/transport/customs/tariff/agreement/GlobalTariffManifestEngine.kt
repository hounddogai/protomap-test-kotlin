package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global tariff manifest calculation. */
interface GlobalTariffManifestStep {
    fun describe(): String
    fun adjust(item: GlobalTariffManifest, value: Double): Double
}

data class MinimumGlobalTariffManifestAdjuster(var minimum: Double) : GlobalTariffManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTariffManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameGlobalTariffManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTariffManifestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalTariffManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalTariffManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalTariffManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalTariffManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalTariffManifestAdjuster(var step: Double) : GlobalTariffManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTariffManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the global tariff manifest steps in order and keeps a trace of each. */
data class GlobalTariffManifestEngine(
    var steps: MutableList<GlobalTariffManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalTariffManifest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTariffManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalTariffManifestEngine(flags: Registry): GlobalTariffManifestEngine {
    return GlobalTariffManifestEngine(
        steps = mutableListOf<GlobalTariffManifestStep>(
            MinimumGlobalTariffManifestAdjuster(28.9),
            NameGlobalTariffManifestAdjuster(
                mutableMapOf<String, Double>("west" to 1.24, "east" to 1.39, "north" to 1.33),
            ),
            FlaggedGlobalTariffManifestAdjuster(flags, "global-tariff-manifest-primary", -6.6),
            RoundingGlobalTariffManifestAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 54459.0,
    )
}
