package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation metric is in its life cycle. */
enum class ViolationMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a violation metric and its current status. */
data class ViolationMetric(
    var code: String,
    var title: String,
    var unit: String,
    var budget: Double,
    var ratio: Double,
    var loadKg: Double,
    var category: String,
    var startsAt: Int,
    var etaMinutes: Int,
    var pallets: Int,
    var stopCount: Int,
    var status: ViolationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationMetricStatus): Boolean {
        when (status) {
            ViolationMetricStatus.REQUESTED -> return next == ViolationMetricStatus.GRANTED || next == ViolationMetricStatus.REVOKED
            ViolationMetricStatus.GRANTED -> {
                when (next) {
                    ViolationMetricStatus.REVOKED -> return true
                }
                return false
            }
            ViolationMetricStatus.REVOKED -> return next == ViolationMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationMetricStatus) {
        if (!canMoveTo(next)) {
            error("violation metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"violation metric"} $code"

    fun projectedExposure(): Double = round((etaMinutes.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ViolationMetric {
        return ViolationMetric(
            code = code,
            title = title,
            unit = unit,
            budget = budget,
            ratio = ratio,
            loadKg = loadKg,
            category = category,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            pallets = pallets,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a violation metric in status requested. */
fun newViolationMetric(code: String, title: String): ViolationMetric {
    return ViolationMetric(
        code = code,
        title = title,
        unit = "",
        budget = 0.0,
        ratio = 0.0,
        loadKg = 0.0,
        category = "",
        startsAt = 0,
        etaMinutes = 0,
        pallets = 0,
        stopCount = 0,
        status = ViolationMetricStatus.REQUESTED,
    )
}
