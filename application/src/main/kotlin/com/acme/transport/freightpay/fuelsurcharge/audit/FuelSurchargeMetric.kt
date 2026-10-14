package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge metric is in its life cycle. */
enum class FuelSurchargeMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fuel surcharge metric, with what the planners need to act on it. */
data class FuelSurchargeMetric(
    var code: String,
    var title: String,
    var graceDays: Int,
    var description: String,
    var depot: String,
    var etaMinutes: Int,
    var channel: String,
    var maximum: Double,
    var attempts: Int,
    var approved: Boolean,
    var status: FuelSurchargeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeMetricStatus): Boolean {
        when (status) {
            FuelSurchargeMetricStatus.NEW -> return next == FuelSurchargeMetricStatus.RUNNING || next == FuelSurchargeMetricStatus.DONE
            FuelSurchargeMetricStatus.QUEUED -> {
                when (next) {
                    FuelSurchargeMetricStatus.RUNNING, FuelSurchargeMetricStatus.FAILED -> return true
                }
                return false
            }
            FuelSurchargeMetricStatus.RUNNING -> return next == FuelSurchargeMetricStatus.DONE || next == FuelSurchargeMetricStatus.FAILED
            FuelSurchargeMetricStatus.DONE -> return next == FuelSurchargeMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeMetricStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((attempts.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeMetric {
        return FuelSurchargeMetric(
            code = code,
            title = title,
            graceDays = graceDays,
            description = description,
            depot = depot,
            etaMinutes = etaMinutes,
            channel = channel,
            maximum = maximum,
            attempts = attempts,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fuel surcharge metric in status new. */
fun newFuelSurchargeMetric(code: String, title: String): FuelSurchargeMetric {
    return FuelSurchargeMetric(
        code = code,
        title = title,
        graceDays = 0,
        description = "",
        depot = "",
        etaMinutes = 0,
        channel = "",
        maximum = 0.0,
        attempts = 0,
        approved = false,
        status = FuelSurchargeMetricStatus.NEW,
    )
}
