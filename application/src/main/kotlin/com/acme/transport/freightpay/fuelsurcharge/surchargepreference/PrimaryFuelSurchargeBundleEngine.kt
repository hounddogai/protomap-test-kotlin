package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary fuel surcharge bundle calculation. */
interface PrimaryFuelSurchargeBundleModifier {
    fun describe(): String
    fun adjust(item: PrimaryFuelSurchargeBundle, value: Double): Double
}

data class TieredPrimaryFuelSurchargeBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryFuelSurchargeBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryFuelSurchargeBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryFuelSurchargeBundleAdjuster(var minimum: Double) : PrimaryFuelSurchargeBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryFuelSurchargeBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryFuelSurchargeBundleBatchSizeOffsetAdjuster(
    var weight: Double,
) : PrimaryFuelSurchargeBundleModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PrimaryFuelSurchargeBundle, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

/** Runs the primary fuel surcharge bundle steps in order and keeps a trace of each. */
data class PrimaryFuelSurchargeBundleEngine(
    var steps: MutableList<PrimaryFuelSurchargeBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryFuelSurchargeBundle): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryFuelSurchargeBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryFuelSurchargeBundleEngine(): PrimaryFuelSurchargeBundleEngine {
    return PrimaryFuelSurchargeBundleEngine(
        steps = mutableListOf<PrimaryFuelSurchargeBundleModifier>(
            TieredPrimaryFuelSurchargeBundleAdjuster(
                mutableListOf<Double>(923.0, 927.0, 7088.0, 9928.0),
                mutableListOf<Double>(1.16, 0.84, 0.9, 1.09),
            ),
            MinimumPrimaryFuelSurchargeBundleAdjuster(45.8),
            PrimaryFuelSurchargeBundleBatchSizeOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 39165.7,
    )
}
