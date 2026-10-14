package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default appointment manifest is in its life cycle. */
enum class DefaultAppointmentManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default appointment manifest, with what the planners need to act on it. */
data class DefaultAppointmentManifest(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var startsAt: Int,
    var category: String,
    var title: String,
    var cost: Double,
    var status: DefaultAppointmentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAppointmentManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAppointmentManifestStatus): Boolean {
        when (status) {
            DefaultAppointmentManifestStatus.REQUESTED -> return next == DefaultAppointmentManifestStatus.GRANTED
            DefaultAppointmentManifestStatus.GRANTED -> {
                when (next) {
                    DefaultAppointmentManifestStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultAppointmentManifestStatus.REVOKED -> {
                when (next) {
                    DefaultAppointmentManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultAppointmentManifestStatus) {
        if (!canMoveTo(next)) {
            error("default appointment manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default appointment manifest"} $code"

    fun effectiveVolume(): Double = round((updatedAt.toDouble() + cost) * 100) / 100

    fun snapshot(): DefaultAppointmentManifest {
        return DefaultAppointmentManifest(
            code = code,
            label = label,
            updatedAt = updatedAt,
            startsAt = startsAt,
            category = category,
            title = title,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default appointment manifest in status requested. */
fun newDefaultAppointmentManifest(code: String, label: String): DefaultAppointmentManifest {
    return DefaultAppointmentManifest(
        code = code,
        label = label,
        updatedAt = 0,
        startsAt = 0,
        category = "",
        title = "",
        cost = 0.0,
        status = DefaultAppointmentManifestStatus.REQUESTED,
    )
}
