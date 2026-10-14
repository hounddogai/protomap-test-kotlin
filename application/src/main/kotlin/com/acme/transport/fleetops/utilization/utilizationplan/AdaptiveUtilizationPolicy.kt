package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive utilization policy is in its life cycle. */
enum class AdaptiveUtilizationPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive utilization policy. Its code is unique within a tenant. */
data class AdaptiveUtilizationPolicy(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var description: String,
    var title: String,
    var country: String,
    var stopCount: Int,
    var odometer: Int,
    var status: AdaptiveUtilizationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveUtilizationPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveUtilizationPolicyStatus): Boolean {
        when (status) {
            AdaptiveUtilizationPolicyStatus.IDLE -> {
                when (next) {
                    AdaptiveUtilizationPolicyStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveUtilizationPolicyStatus.WARMING -> {
                when (next) {
                    AdaptiveUtilizationPolicyStatus.DRAINING, AdaptiveUtilizationPolicyStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveUtilizationPolicyStatus.READY -> {
                when (next) {
                    AdaptiveUtilizationPolicyStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveUtilizationPolicyStatus.DRAINING -> return next == AdaptiveUtilizationPolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveUtilizationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive utilization policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round(stopCount.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): AdaptiveUtilizationPolicy {
        return AdaptiveUtilizationPolicy(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            description = description,
            title = title,
            country = country,
            stopCount = stopCount,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive utilization policy in status idle. */
fun newAdaptiveUtilizationPolicy(code: String, label: String): AdaptiveUtilizationPolicy {
    return AdaptiveUtilizationPolicy(
        code = code,
        label = label,
        fuelLiters = 0.0,
        description = "",
        title = "",
        country = "",
        stopCount = 0,
        odometer = 0,
        status = AdaptiveUtilizationPolicyStatus.IDLE,
    )
}
