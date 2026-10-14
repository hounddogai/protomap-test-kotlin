package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.text.normalize

/** Where a appointment digest is in its life cycle. */
enum class AppointmentDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A appointment digest. Its code is unique within a tenant. */
data class AppointmentDigest(
    var code: String,
    var name: String,
    var sequence: Int,
    var vehicleId: String,
    var country: String,
    var status: AppointmentDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentDigestStatus): Boolean {
        when (status) {
            AppointmentDigestStatus.REQUESTED -> {
                when (next) {
                    AppointmentDigestStatus.GRANTED, AppointmentDigestStatus.EXPIRED -> return true
                }
                return false
            }
            AppointmentDigestStatus.GRANTED -> return next == AppointmentDigestStatus.REVOKED || next == AppointmentDigestStatus.EXPIRED
            AppointmentDigestStatus.REVOKED -> return next == AppointmentDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AppointmentDigestStatus) {
        if (!canMoveTo(next)) {
            error("appointment digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): AppointmentDigest {
        return AppointmentDigest(
            code = code,
            name = name,
            sequence = sequence,
            vehicleId = vehicleId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a appointment digest in status requested. */
fun newAppointmentDigest(code: String, name: String): AppointmentDigest {
    return AppointmentDigest(
        code = code,
        name = name,
        sequence = 0,
        vehicleId = "",
        country = "",
        status = AppointmentDigestStatus.REQUESTED,
    )
}
