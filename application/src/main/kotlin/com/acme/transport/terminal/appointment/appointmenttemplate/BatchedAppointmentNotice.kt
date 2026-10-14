package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.text.normalize

/** Where a batched appointment notice is in its life cycle. */
enum class BatchedAppointmentNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched appointment notice. Its code is unique within a tenant. */
data class BatchedAppointmentNotice(
    var code: String,
    var label: String,
    var currency: String,
    var routeCode: String,
    var unit: String,
    var timezone: String,
    var category: String,
    var phone: String,
    var quantity: Int,
    var status: BatchedAppointmentNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAppointmentNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAppointmentNoticeStatus): Boolean {
        when (status) {
            BatchedAppointmentNoticeStatus.CREATED -> return next == BatchedAppointmentNoticeStatus.PUBLISHED
            BatchedAppointmentNoticeStatus.VALIDATED -> return next == BatchedAppointmentNoticeStatus.RETIRED
            BatchedAppointmentNoticeStatus.PUBLISHED -> {
                when (next) {
                    BatchedAppointmentNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAppointmentNoticeStatus) {
        if (!canMoveTo(next)) {
            error("batched appointment notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): BatchedAppointmentNotice {
        return BatchedAppointmentNotice(
            code = code,
            label = label,
            currency = currency,
            routeCode = routeCode,
            unit = unit,
            timezone = timezone,
            category = category,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched appointment notice in status created. */
fun newBatchedAppointmentNotice(code: String, label: String): BatchedAppointmentNotice {
    return BatchedAppointmentNotice(
        code = code,
        label = label,
        currency = "",
        routeCode = "",
        unit = "",
        timezone = "",
        category = "",
        phone = "",
        quantity = 0,
        status = BatchedAppointmentNoticeStatus.CREATED,
    )
}
