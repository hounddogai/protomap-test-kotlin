package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan forecast is in its life cycle. */
enum class ServicePlanForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan forecast as the service plan team keeps it. */
data class ServicePlanForecast(
    var code: String,
    var label: String,
    var amount: Double,
    var country: String,
    var lane: String,
    var weight: Double,
    var status: ServicePlanForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanForecastStatus): Boolean {
        when (status) {
            ServicePlanForecastStatus.CREATED -> {
                when (next) {
                    ServicePlanForecastStatus.PUBLISHED, ServicePlanForecastStatus.RETIRED -> return true
                }
                return false
            }
            ServicePlanForecastStatus.VALIDATED -> return next == ServicePlanForecastStatus.PUBLISHED
            ServicePlanForecastStatus.PUBLISHED -> {
                when (next) {
                    ServicePlanForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanForecastStatus) {
        if (!canMoveTo(next)) {
            error("service plan forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((amount + weight) * 100) / 100

    fun snapshot(): ServicePlanForecast {
        return ServicePlanForecast(
            code = code,
            label = label,
            amount = amount,
            country = country,
            lane = lane,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a service plan forecast in status created. */
fun newServicePlanForecast(code: String, label: String): ServicePlanForecast {
    return ServicePlanForecast(
        code = code,
        label = label,
        amount = 0.0,
        country = "",
        lane = "",
        weight = 0.0,
        status = ServicePlanForecastStatus.CREATED,
    )
}
