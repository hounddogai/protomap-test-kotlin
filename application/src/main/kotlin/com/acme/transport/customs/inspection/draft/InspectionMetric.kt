package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection metric is in its life cycle. */
enum class InspectionMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One inspection metric, with what the planners need to act on it. */
data class InspectionMetric(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var odometer: Int,
    var amount: Double,
    var fuelLiters: Double,
    var country: String,
    var status: InspectionMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionMetricStatus): Boolean {
        when (status) {
            InspectionMetricStatus.NEW -> {
                when (next) {
                    InspectionMetricStatus.QUEUED -> return true
                }
                return false
            }
            InspectionMetricStatus.QUEUED -> {
                when (next) {
                    InspectionMetricStatus.RUNNING, InspectionMetricStatus.DONE -> return true
                }
                return false
            }
            InspectionMetricStatus.RUNNING -> return next == InspectionMetricStatus.FAILED
            InspectionMetricStatus.DONE -> {
                when (next) {
                    InspectionMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionMetricStatus) {
        if (!canMoveTo(next)) {
            error("inspection metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedTotal(): Double = round((fuelLiters + amount) * 100) / 100

    fun snapshot(): InspectionMetric {
        return InspectionMetric(
            code = code,
            label = label,
            pinned = pinned,
            odometer = odometer,
            amount = amount,
            fuelLiters = fuelLiters,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a inspection metric in status new. */
fun newInspectionMetric(code: String, label: String): InspectionMetric {
    return InspectionMetric(
        code = code,
        label = label,
        pinned = false,
        odometer = 0,
        amount = 0.0,
        fuelLiters = 0.0,
        country = "",
        status = InspectionMetricStatus.NEW,
    )
}
