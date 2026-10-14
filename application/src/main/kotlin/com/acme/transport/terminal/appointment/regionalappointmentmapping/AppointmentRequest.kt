package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.text.normalize

/** Where a appointment request is in its life cycle. */
enum class AppointmentRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a appointment request and its current status. */
data class AppointmentRequest(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var depot: String,
    var trailerId: String,
    var country: String,
    var category: String,
    var notes: String,
    var status: AppointmentRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentRequestStatus): Boolean {
        when (status) {
            AppointmentRequestStatus.CREATED -> {
                when (next) {
                    AppointmentRequestStatus.VALIDATED -> return true
                }
                return false
            }
            AppointmentRequestStatus.VALIDATED -> return next == AppointmentRequestStatus.RETIRED
            AppointmentRequestStatus.PUBLISHED -> {
                when (next) {
                    AppointmentRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentRequestStatus) {
        if (!canMoveTo(next)) {
            error("appointment request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): AppointmentRequest {
        return AppointmentRequest(
            code = code,
            title = title,
            updatedAt = updatedAt,
            depot = depot,
            trailerId = trailerId,
            country = country,
            category = category,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a appointment request in status created. */
fun newAppointmentRequest(code: String, title: String): AppointmentRequest {
    return AppointmentRequest(
        code = code,
        title = title,
        updatedAt = 0,
        depot = "",
        trailerId = "",
        country = "",
        category = "",
        notes = "",
        status = AppointmentRequestStatus.CREATED,
    )
}
