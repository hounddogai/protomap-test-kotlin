package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment quota is in its life cycle. */
enum class AppointmentQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A appointment quota as the appointment team keeps it. */
data class AppointmentQuota(
    var code: String,
    var title: String,
    var sequence: Int,
    var pinned: Boolean,
    var revision: Int,
    var ceiling: Double,
    var createdAt: Int,
    var status: AppointmentQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentQuotaStatus): Boolean {
        when (status) {
            AppointmentQuotaStatus.PLANNED -> {
                when (next) {
                    AppointmentQuotaStatus.SCHEDULED, AppointmentQuotaStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AppointmentQuotaStatus.SCHEDULED -> {
                when (next) {
                    AppointmentQuotaStatus.IN_PROGRESS, AppointmentQuotaStatus.COMPLETED -> return true
                }
                return false
            }
            AppointmentQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    AppointmentQuotaStatus.COMPLETED, AppointmentQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentQuotaStatus.COMPLETED -> {
                when (next) {
                    AppointmentQuotaStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentQuotaStatus) {
        if (!canMoveTo(next)) {
            error("appointment quota $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round(revision.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): AppointmentQuota {
        return AppointmentQuota(
            code = code,
            title = title,
            sequence = sequence,
            pinned = pinned,
            revision = revision,
            ceiling = ceiling,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment quota in status planned. */
fun newAppointmentQuota(code: String, title: String): AppointmentQuota {
    return AppointmentQuota(
        code = code,
        title = title,
        sequence = 0,
        pinned = false,
        revision = 0,
        ceiling = 0.0,
        createdAt = 0,
        status = AppointmentQuotaStatus.PLANNED,
    )
}
