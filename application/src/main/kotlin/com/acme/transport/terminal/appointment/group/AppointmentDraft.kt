package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment draft is in its life cycle. */
enum class AppointmentDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A appointment draft as the appointment team keeps it. */
data class AppointmentDraft(
    var code: String,
    var name: String,
    var attempts: Int,
    var cost: Double,
    var externalRef: String,
    var title: String,
    var phone: String,
    var threshold: Double,
    var status: AppointmentDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentDraftStatus): Boolean {
        when (status) {
            AppointmentDraftStatus.CREATED -> return next == AppointmentDraftStatus.RETIRED
            AppointmentDraftStatus.VALIDATED -> return next == AppointmentDraftStatus.PUBLISHED
            AppointmentDraftStatus.PUBLISHED -> {
                when (next) {
                    AppointmentDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentDraftStatus) {
        if (!canMoveTo(next)) {
            error("appointment draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment draft"} $code"

    fun netTotal(): Double = round((cost + threshold) * 100) / 100

    fun snapshot(): AppointmentDraft {
        return AppointmentDraft(
            code = code,
            name = name,
            attempts = attempts,
            cost = cost,
            externalRef = externalRef,
            title = title,
            phone = phone,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment draft in status created. */
fun newAppointmentDraft(code: String, name: String): AppointmentDraft {
    return AppointmentDraft(
        code = code,
        name = name,
        attempts = 0,
        cost = 0.0,
        externalRef = "",
        title = "",
        phone = "",
        threshold = 0.0,
        status = AppointmentDraftStatus.CREATED,
    )
}
