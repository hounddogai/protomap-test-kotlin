package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service area record calculation. */
interface ServiceAreaRecordModifier {
    fun describe(): String
    fun adjust(item: ServiceAreaRecord, value: Double): Double
}

data class TieredServiceAreaRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapServiceAreaRecordAdjuster(var cap: Double) : ServiceAreaRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaRecord, value: Double): Double = minOf(value, cap)
}

data class ChannelServiceAreaRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : ServiceAreaRecordModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ServiceAreaRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedServiceAreaRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentServiceAreaRecordAdjuster(var percent: Double) : ServiceAreaRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service area record steps in order and keeps a trace of each. */
data class ServiceAreaRecordEngine(
    var steps: MutableList<ServiceAreaRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaRecord): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaRecordEngine(flags: Registry): ServiceAreaRecordEngine {
    return ServiceAreaRecordEngine(
        steps = mutableListOf<ServiceAreaRecordModifier>(
            TieredServiceAreaRecordAdjuster(
                mutableListOf<Double>(1376.0, 3584.0, 3668.0, 5834.0),
                mutableListOf<Double>(0.91, 1.11, 0.83, 1.03),
            ),
            CapServiceAreaRecordAdjuster(19954.0),
            ChannelServiceAreaRecordAdjuster(mutableMapOf<String, Double>("south" to 1.0)),
            FlaggedServiceAreaRecordAdjuster(flags, "service-area-record-adaptive", -2.6),
            PercentServiceAreaRecordAdjuster(4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 99676.8,
    )
}
