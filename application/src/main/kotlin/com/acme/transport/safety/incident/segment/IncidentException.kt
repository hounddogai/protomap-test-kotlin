package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident exception is in its life cycle. */
enum class IncidentExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A incident exception as the incident team keeps it. */
data class IncidentException(
    var code: String,
    var label: String,
    var budget: Double,
    var routeCode: String,
    var fuelLiters: Double,
    var loadKg: Double,
    var trailerId: String,
    var status: IncidentExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentExceptionStatus): Boolean {
        when (status) {
            IncidentExceptionStatus.CREATED -> {
                when (next) {
                    IncidentExceptionStatus.RETIRED -> return true
                }
                return false
            }
            IncidentExceptionStatus.VALIDATED -> {
                when (next) {
                    IncidentExceptionStatus.PUBLISHED -> return true
                }
                return false
            }
            IncidentExceptionStatus.PUBLISHED -> return next == IncidentExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IncidentExceptionStatus) {
        if (!canMoveTo(next)) {
            error("incident exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((fuelLiters - loadKg) * 100) / 100

    fun snapshot(): IncidentException {
        return IncidentException(
            code = code,
            label = label,
            budget = budget,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident exception in status created. */
fun newIncidentException(code: String, label: String): IncidentException {
    return IncidentException(
        code = code,
        label = label,
        budget = 0.0,
        routeCode = "",
        fuelLiters = 0.0,
        loadKg = 0.0,
        trailerId = "",
        status = IncidentExceptionStatus.CREATED,
    )
}
