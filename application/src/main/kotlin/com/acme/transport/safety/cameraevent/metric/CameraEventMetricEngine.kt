package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the camera event metric calculation. */
interface CameraEventMetricStep {
    fun describe(): String
    fun adjust(item: CameraEventMetric, value: Double): Double
}

data class CameraEventMetricFuelLitersOffsetAdjuster(var weight: Double) : CameraEventMetricStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: CameraEventMetric, value: Double): Double = value + item.fuelLiters * weight
}

data class NameCameraEventMetricAdjuster(var factors: MutableMap<String, Double>) : CameraEventMetricStep {
    override fun describe(): String = "by name"

    override fun adjust(item: CameraEventMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCameraEventMetricAdjuster(var step: Double) : CameraEventMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventMetric, value: Double): Double = roundTo(value, step)
}

data class TieredCameraEventMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the camera event metric steps in order and keeps a trace of each. */
data class CameraEventMetricEngine(
    var steps: MutableList<CameraEventMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventMetricEngine(): CameraEventMetricEngine {
    return CameraEventMetricEngine(
        steps = mutableListOf<CameraEventMetricStep>(
            CameraEventMetricFuelLitersOffsetAdjuster(0.96),
            NameCameraEventMetricAdjuster(mutableMapOf<String, Double>("north" to 0.9)),
            RoundingCameraEventMetricAdjuster(0.1),
            TieredCameraEventMetricAdjuster(
                mutableListOf<Double>(3791.0, 8031.0, 9600.0),
                mutableListOf<Double>(0.91, 1.13, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 77063.9,
    )
}
