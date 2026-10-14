package com.acme.transport.customs.tariff.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tariff quota calculation. */
interface ManualTariffQuotaStep {
    fun describe(): String
    fun adjust(item: ManualTariffQuota, value: Double): Double
}

data class LocaleManualTariffQuotaAdjuster(var factors: MutableMap<String, Double>) : ManualTariffQuotaStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualTariffQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualTariffQuotaAdjuster(var cap: Double) : ManualTariffQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTariffQuota, value: Double): Double = minOf(value, cap)
}

data class ManualTariffQuotaBatchSizeOffsetAdjuster(var weight: Double) : ManualTariffQuotaStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ManualTariffQuota, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class FlaggedManualTariffQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTariffQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTariffQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualTariffQuotaAdjuster(var percent: Double) : ManualTariffQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTariffQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualTariffQuotaAdjuster(var step: Double) : ManualTariffQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTariffQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the manual tariff quota steps in order and keeps a trace of each. */
data class ManualTariffQuotaEngine(
    var steps: MutableList<ManualTariffQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTariffQuota): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTariffQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTariffQuotaEngine(flags: Registry): ManualTariffQuotaEngine {
    return ManualTariffQuotaEngine(
        steps = mutableListOf<ManualTariffQuotaStep>(
            LocaleManualTariffQuotaAdjuster(mutableMapOf<String, Double>("central" to 1.09, "retail" to 1.38)),
            CapManualTariffQuotaAdjuster(30915.0),
            ManualTariffQuotaBatchSizeOffsetAdjuster(1.78),
            FlaggedManualTariffQuotaAdjuster(flags, "manual-tariff-quota-adaptive", 8.3),
            PercentManualTariffQuotaAdjuster(13.4),
            RoundingManualTariffQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 91909.5,
    )
}
