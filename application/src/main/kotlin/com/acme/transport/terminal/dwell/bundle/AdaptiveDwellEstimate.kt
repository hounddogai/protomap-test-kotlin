package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dwell estimate is in its life cycle. */
enum class AdaptiveDwellEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive dwell estimate as the dwell team keeps it. */
data class AdaptiveDwellEstimate(
    var code: String,
    var label: String,
    var routeCode: String,
    var odometer: Int,
    var discount: Double,
    var status: AdaptiveDwellEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDwellEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDwellEstimateStatus): Boolean {
        when (status) {
            AdaptiveDwellEstimateStatus.NEW -> return next == AdaptiveDwellEstimateStatus.RUNNING
            AdaptiveDwellEstimateStatus.QUEUED -> return next == AdaptiveDwellEstimateStatus.FAILED
            AdaptiveDwellEstimateStatus.RUNNING -> return next == AdaptiveDwellEstimateStatus.DONE || next == AdaptiveDwellEstimateStatus.FAILED
            AdaptiveDwellEstimateStatus.DONE -> {
                when (next) {
                    AdaptiveDwellEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDwellEstimateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dwell estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((odometer.toDouble() + discount) * 100) / 100

    fun snapshot(): AdaptiveDwellEstimate {
        return AdaptiveDwellEstimate(
            code = code,
            label = label,
            routeCode = routeCode,
            odometer = odometer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive dwell estimate in status new. */
fun newAdaptiveDwellEstimate(code: String, label: String): AdaptiveDwellEstimate {
    return AdaptiveDwellEstimate(
        code = code,
        label = label,
        routeCode = "",
        odometer = 0,
        discount = 0.0,
        status = AdaptiveDwellEstimateStatus.NEW,
    )
}
