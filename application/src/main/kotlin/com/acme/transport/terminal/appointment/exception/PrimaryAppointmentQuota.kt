package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.text.normalize

/** Where a primary appointment quota is in its life cycle. */
enum class PrimaryAppointmentQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary appointment quota as the appointment team keeps it. */
data class PrimaryAppointmentQuota(
    var code: String,
    var name: String,
    var retries: Int,
    var dock: String,
    var summary: String,
    var status: PrimaryAppointmentQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAppointmentQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAppointmentQuotaStatus): Boolean {
        when (status) {
            PrimaryAppointmentQuotaStatus.NEW -> {
                when (next) {
                    PrimaryAppointmentQuotaStatus.RUNNING, PrimaryAppointmentQuotaStatus.DONE -> return true
                }
                return false
            }
            PrimaryAppointmentQuotaStatus.QUEUED -> {
                when (next) {
                    PrimaryAppointmentQuotaStatus.RUNNING, PrimaryAppointmentQuotaStatus.FAILED -> return true
                }
                return false
            }
            PrimaryAppointmentQuotaStatus.RUNNING -> return next == PrimaryAppointmentQuotaStatus.FAILED
            PrimaryAppointmentQuotaStatus.DONE -> return next == PrimaryAppointmentQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryAppointmentQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary appointment quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): PrimaryAppointmentQuota {
        return PrimaryAppointmentQuota(
            code = code,
            name = name,
            retries = retries,
            dock = dock,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary appointment quota in status new. */
fun newPrimaryAppointmentQuota(code: String, name: String): PrimaryAppointmentQuota {
    return PrimaryAppointmentQuota(
        code = code,
        name = name,
        retries = 0,
        dock = "",
        summary = "",
        status = PrimaryAppointmentQuotaStatus.NEW,
    )
}
