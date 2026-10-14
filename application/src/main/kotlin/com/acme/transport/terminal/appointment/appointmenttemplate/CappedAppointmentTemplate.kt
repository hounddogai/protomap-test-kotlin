package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped appointment template records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a capped appointment template is in its life cycle. */
enum class CappedAppointmentTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capped appointment template, with what the planners need to act on it. */
data class CappedAppointmentTemplate(
    var code: String,
    var label: String,
    var active: Boolean,
    var currency: String,
    var timezone: String,
    var limit: Int,
    var threshold: Double,
    var lane: String,
    var status: CappedAppointmentTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAppointmentTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAppointmentTemplateStatus): Boolean {
        when (status) {
            CappedAppointmentTemplateStatus.REQUESTED -> {
                when (next) {
                    CappedAppointmentTemplateStatus.GRANTED, CappedAppointmentTemplateStatus.EXPIRED -> return true
                }
                return false
            }
            CappedAppointmentTemplateStatus.GRANTED -> return next == CappedAppointmentTemplateStatus.REVOKED || next == CappedAppointmentTemplateStatus.EXPIRED
            CappedAppointmentTemplateStatus.REVOKED -> {
                when (next) {
                    CappedAppointmentTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAppointmentTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped appointment template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedValue(): Double = round((limit.toDouble() + threshold) * 100) / 100

    fun snapshot(): CappedAppointmentTemplate {
        return CappedAppointmentTemplate(
            code = code,
            label = label,
            active = active,
            currency = currency,
            timezone = timezone,
            limit = limit,
            threshold = threshold,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped appointment template in status requested. */
fun newCappedAppointmentTemplate(code: String, label: String): CappedAppointmentTemplate {
    return CappedAppointmentTemplate(
        code = code,
        label = label,
        active = false,
        currency = "",
        timezone = "",
        limit = 0,
        threshold = 0.0,
        lane = "",
        status = CappedAppointmentTemplateStatus.REQUESTED,
    )
}
