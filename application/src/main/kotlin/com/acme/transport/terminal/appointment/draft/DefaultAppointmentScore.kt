package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.text.normalize

/** Where a default appointment score is in its life cycle. */
enum class DefaultAppointmentScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default appointment score and its current status. */
data class DefaultAppointmentScore(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var carrier: String,
    var visible: Boolean,
    var title: String,
    var status: DefaultAppointmentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAppointmentScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAppointmentScoreStatus): Boolean {
        when (status) {
            DefaultAppointmentScoreStatus.PLANNED -> {
                when (next) {
                    DefaultAppointmentScoreStatus.COMPLETED, DefaultAppointmentScoreStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultAppointmentScoreStatus.SCHEDULED -> {
                when (next) {
                    DefaultAppointmentScoreStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultAppointmentScoreStatus.IN_PROGRESS -> return next == DefaultAppointmentScoreStatus.COMPLETED || next == DefaultAppointmentScoreStatus.CANCELLED
            DefaultAppointmentScoreStatus.COMPLETED -> return next == DefaultAppointmentScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultAppointmentScoreStatus) {
        if (!canMoveTo(next)) {
            error("default appointment score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): DefaultAppointmentScore {
        return DefaultAppointmentScore(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            carrier = carrier,
            visible = visible,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default appointment score in status planned. */
fun newDefaultAppointmentScore(code: String, label: String): DefaultAppointmentScore {
    return DefaultAppointmentScore(
        code = code,
        label = label,
        etaMinutes = 0,
        carrier = "",
        visible = false,
        title = "",
        status = DefaultAppointmentScoreStatus.PLANNED,
    )
}
