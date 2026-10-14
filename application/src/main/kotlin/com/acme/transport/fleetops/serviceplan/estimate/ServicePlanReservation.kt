package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan reservation is in its life cycle. */
enum class ServicePlanReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service plan reservation. Its code is unique within a tenant. */
data class ServicePlanReservation(
    var code: String,
    var name: String,
    var margin: Double,
    var amount: Double,
    var durationMinutes: Int,
    var batchSize: Int,
    var fuelLiters: Double,
    var status: ServicePlanReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanReservationStatus): Boolean {
        when (status) {
            ServicePlanReservationStatus.REQUESTED -> return next == ServicePlanReservationStatus.EXPIRED
            ServicePlanReservationStatus.GRANTED -> {
                when (next) {
                    ServicePlanReservationStatus.REVOKED, ServicePlanReservationStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanReservationStatus.REVOKED -> return next == ServicePlanReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanReservationStatus) {
        if (!canMoveTo(next)) {
            error("service plan reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((fuelLiters + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanReservation {
        return ServicePlanReservation(
            code = code,
            name = name,
            margin = margin,
            amount = amount,
            durationMinutes = durationMinutes,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan reservation in status requested. */
fun newServicePlanReservation(code: String, name: String): ServicePlanReservation {
    return ServicePlanReservation(
        code = code,
        name = name,
        margin = 0.0,
        amount = 0.0,
        durationMinutes = 0,
        batchSize = 0,
        fuelLiters = 0.0,
        status = ServicePlanReservationStatus.REQUESTED,
    )
}
