package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal appointment audit is in its life cycle. */
enum class SeasonalAppointmentAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal appointment audit as the appointment team keeps it. */
data class SeasonalAppointmentAudit(
    var code: String,
    var label: String,
    var revision: Int,
    var score: Double,
    var vehicleId: String,
    var quantity: Int,
    var segment: String,
    var margin: Double,
    var capacity: Int,
    var status: SeasonalAppointmentAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAppointmentAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAppointmentAuditStatus): Boolean {
        when (status) {
            SeasonalAppointmentAuditStatus.REQUESTED -> {
                when (next) {
                    SeasonalAppointmentAuditStatus.GRANTED -> return true
                }
                return false
            }
            SeasonalAppointmentAuditStatus.GRANTED -> return next == SeasonalAppointmentAuditStatus.REVOKED
            SeasonalAppointmentAuditStatus.REVOKED -> return next == SeasonalAppointmentAuditStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalAppointmentAuditStatus) {
        if (!canMoveTo(next)) {
            error("seasonal appointment audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"seasonal appointment audit"} $code"

    fun adjustedTotal(): Double = round((quantity.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalAppointmentAudit {
        return SeasonalAppointmentAudit(
            code = code,
            label = label,
            revision = revision,
            score = score,
            vehicleId = vehicleId,
            quantity = quantity,
            segment = segment,
            margin = margin,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal appointment audit in status requested. */
fun newSeasonalAppointmentAudit(code: String, label: String): SeasonalAppointmentAudit {
    return SeasonalAppointmentAudit(
        code = code,
        label = label,
        revision = 0,
        score = 0.0,
        vehicleId = "",
        quantity = 0,
        segment = "",
        margin = 0.0,
        capacity = 0,
        status = SeasonalAppointmentAuditStatus.REQUESTED,
    )
}
