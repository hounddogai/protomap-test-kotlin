package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual violation metric records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a manual violation metric is in its life cycle. */
enum class ManualViolationMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual violation metric. Its code is unique within a tenant. */
data class ManualViolationMetric(
    var code: String,
    var label: String,
    var endsAt: Int,
    var startsAt: Int,
    var fuelLiters: Double,
    var margin: Double,
    var cost: Double,
    var unit: String,
    var locale: String,
    var status: ManualViolationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualViolationMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualViolationMetricStatus): Boolean {
        when (status) {
            ManualViolationMetricStatus.NEW -> return next == ManualViolationMetricStatus.QUEUED
            ManualViolationMetricStatus.QUEUED -> return next == ManualViolationMetricStatus.RUNNING
            ManualViolationMetricStatus.RUNNING -> {
                when (next) {
                    ManualViolationMetricStatus.DONE, ManualViolationMetricStatus.FAILED -> return true
                }
                return false
            }
            ManualViolationMetricStatus.DONE -> return next == ManualViolationMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualViolationMetricStatus) {
        if (!canMoveTo(next)) {
            error("manual violation metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round((endsAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): ManualViolationMetric {
        return ManualViolationMetric(
            code = code,
            label = label,
            endsAt = endsAt,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            margin = margin,
            cost = cost,
            unit = unit,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual violation metric in status new. */
fun newManualViolationMetric(code: String, label: String): ManualViolationMetric {
    return ManualViolationMetric(
        code = code,
        label = label,
        endsAt = 0,
        startsAt = 0,
        fuelLiters = 0.0,
        margin = 0.0,
        cost = 0.0,
        unit = "",
        locale = "",
        status = ManualViolationMetricStatus.NEW,
    )
}
