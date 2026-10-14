package com.acme.transport.network.servicearea.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area reservation is in its life cycle. */
enum class ServiceAreaReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service area reservation. Its code is unique within a tenant. */
data class ServiceAreaReservation(
    var code: String,
    var label: String,
    var rate: Double,
    var maximum: Double,
    var odometer: Int,
    var vehicleId: String,
    var contactEmail: String,
    var updatedAt: Int,
    var quantity: Int,
    var pinned: Boolean,
    var status: ServiceAreaReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaReservationStatus): Boolean {
        when (status) {
            ServiceAreaReservationStatus.OPEN -> {
                when (next) {
                    ServiceAreaReservationStatus.RESOLVED, ServiceAreaReservationStatus.REOPENED -> return true
                }
                return false
            }
            ServiceAreaReservationStatus.IN_REVIEW -> return next == ServiceAreaReservationStatus.REOPENED || next == ServiceAreaReservationStatus.ARCHIVED
            ServiceAreaReservationStatus.RESOLVED -> return next == ServiceAreaReservationStatus.REOPENED || next == ServiceAreaReservationStatus.ARCHIVED
            ServiceAreaReservationStatus.REOPENED -> {
                when (next) {
                    ServiceAreaReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaReservationStatus) {
        if (!canMoveTo(next)) {
            error("service area reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((odometer.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaReservation {
        return ServiceAreaReservation(
            code = code,
            label = label,
            rate = rate,
            maximum = maximum,
            odometer = odometer,
            vehicleId = vehicleId,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            quantity = quantity,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service area reservation in status open. */
fun newServiceAreaReservation(code: String, label: String): ServiceAreaReservation {
    return ServiceAreaReservation(
        code = code,
        label = label,
        rate = 0.0,
        maximum = 0.0,
        odometer = 0,
        vehicleId = "",
        contactEmail = "",
        updatedAt = 0,
        quantity = 0,
        pinned = false,
        status = ServiceAreaReservationStatus.OPEN,
    )
}
