package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan variance is in its life cycle. */
enum class ServicePlanVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a service plan variance and its current status. */
data class ServicePlanVariance(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var carrier: String,
    var fuelLiters: Double,
    var createdAt: Int,
    var source: String,
    var ratio: Double,
    var capacity: Int,
    var threshold: Double,
    var status: ServicePlanVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanVarianceStatus): Boolean {
        when (status) {
            ServicePlanVarianceStatus.PROPOSED -> {
                when (next) {
                    ServicePlanVarianceStatus.REJECTED, ServicePlanVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServicePlanVarianceStatus.APPROVED -> {
                when (next) {
                    ServicePlanVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServicePlanVarianceStatus.REJECTED -> {
                when (next) {
                    ServicePlanVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanVarianceStatus) {
        if (!canMoveTo(next)) {
            error("service plan variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((ratio - threshold) * 100) / 100

    fun snapshot(): ServicePlanVariance {
        return ServicePlanVariance(
            code = code,
            label = label,
            attributes = attributes,
            carrier = carrier,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            source = source,
            ratio = ratio,
            capacity = capacity,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a service plan variance in status proposed. */
fun newServicePlanVariance(code: String, label: String): ServicePlanVariance {
    return ServicePlanVariance(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        carrier = "",
        fuelLiters = 0.0,
        createdAt = 0,
        source = "",
        ratio = 0.0,
        capacity = 0,
        threshold = 0.0,
        status = ServicePlanVarianceStatus.PROPOSED,
    )
}
