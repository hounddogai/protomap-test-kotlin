package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment manifest is in its life cycle. */
enum class AppointmentManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A appointment manifest. Its code is unique within a tenant. */
data class AppointmentManifest(
    var code: String,
    var name: String,
    var createdAt: Int,
    var notes: String,
    var shiftCode: String,
    var country: String,
    var pinned: Boolean,
    var source: String,
    var cost: Double,
    var status: AppointmentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentManifestStatus): Boolean {
        when (status) {
            AppointmentManifestStatus.NEW -> return next == AppointmentManifestStatus.RUNNING || next == AppointmentManifestStatus.FAILED
            AppointmentManifestStatus.QUEUED -> return next == AppointmentManifestStatus.RUNNING || next == AppointmentManifestStatus.DONE
            AppointmentManifestStatus.RUNNING -> {
                when (next) {
                    AppointmentManifestStatus.FAILED -> return true
                }
                return false
            }
            AppointmentManifestStatus.DONE -> return next == AppointmentManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AppointmentManifestStatus) {
        if (!canMoveTo(next)) {
            error("appointment manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round(createdAt.toDouble() * cost * 100) / 100

    fun snapshot(): AppointmentManifest {
        return AppointmentManifest(
            code = code,
            name = name,
            createdAt = createdAt,
            notes = notes,
            shiftCode = shiftCode,
            country = country,
            pinned = pinned,
            source = source,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a appointment manifest in status new. */
fun newAppointmentManifest(code: String, name: String): AppointmentManifest {
    return AppointmentManifest(
        code = code,
        name = name,
        createdAt = 0,
        notes = "",
        shiftCode = "",
        country = "",
        pinned = false,
        source = "",
        cost = 0.0,
        status = AppointmentManifestStatus.NEW,
    )
}
