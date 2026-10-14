package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.normalize

// The appointment template records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment template is in its life cycle. */
enum class AppointmentTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One appointment template, with what the planners need to act on it. */
data class AppointmentTemplate(
    var code: String,
    var label: String,
    var dock: String,
    var source: String,
    var timezone: String,
    var title: String,
    var unit: String,
    var checksum: String,
    var phone: String,
    var segment: String,
    var amount: Double,
    var status: AppointmentTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentTemplateStatus): Boolean {
        when (status) {
            AppointmentTemplateStatus.REQUESTED -> return next == AppointmentTemplateStatus.REVOKED || next == AppointmentTemplateStatus.EXPIRED
            AppointmentTemplateStatus.GRANTED -> return next == AppointmentTemplateStatus.REVOKED || next == AppointmentTemplateStatus.EXPIRED
            AppointmentTemplateStatus.REVOKED -> return next == AppointmentTemplateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AppointmentTemplateStatus) {
        if (!canMoveTo(next)) {
            error("appointment template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): AppointmentTemplate {
        return AppointmentTemplate(
            code = code,
            label = label,
            dock = dock,
            source = source,
            timezone = timezone,
            title = title,
            unit = unit,
            checksum = checksum,
            phone = phone,
            segment = segment,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a appointment template in status requested. */
fun newAppointmentTemplate(code: String, label: String): AppointmentTemplate {
    return AppointmentTemplate(
        code = code,
        label = label,
        dock = "",
        source = "",
        timezone = "",
        title = "",
        unit = "",
        checksum = "",
        phone = "",
        segment = "",
        amount = 0.0,
        status = AppointmentTemplateStatus.REQUESTED,
    )
}
