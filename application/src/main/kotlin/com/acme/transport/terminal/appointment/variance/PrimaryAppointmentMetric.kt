package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize

/** Where a primary appointment metric is in its life cycle. */
enum class PrimaryAppointmentMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary appointment metric and its current status. */
data class PrimaryAppointmentMetric(
    var code: String,
    var label: String,
    var ownerId: String,
    var locale: String,
    var country: String,
    var distanceKm: Double,
    var status: PrimaryAppointmentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAppointmentMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAppointmentMetricStatus): Boolean {
        when (status) {
            PrimaryAppointmentMetricStatus.REQUESTED -> {
                when (next) {
                    PrimaryAppointmentMetricStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryAppointmentMetricStatus.GRANTED -> return next == PrimaryAppointmentMetricStatus.REVOKED
            PrimaryAppointmentMetricStatus.REVOKED -> {
                when (next) {
                    PrimaryAppointmentMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAppointmentMetricStatus) {
        if (!canMoveTo(next)) {
            error("primary appointment metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): PrimaryAppointmentMetric {
        return PrimaryAppointmentMetric(
            code = code,
            label = label,
            ownerId = ownerId,
            locale = locale,
            country = country,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary appointment metric in status requested. */
fun newPrimaryAppointmentMetric(code: String, label: String): PrimaryAppointmentMetric {
    return PrimaryAppointmentMetric(
        code = code,
        label = label,
        ownerId = "",
        locale = "",
        country = "",
        distanceKm = 0.0,
        status = PrimaryAppointmentMetricStatus.REQUESTED,
    )
}
