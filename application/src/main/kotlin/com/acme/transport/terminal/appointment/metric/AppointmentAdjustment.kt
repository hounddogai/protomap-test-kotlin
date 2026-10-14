package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment adjustment is in its life cycle. */
enum class AppointmentAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a appointment adjustment and its current status. */
data class AppointmentAdjustment(
    var code: String,
    var label: String,
    var name: String,
    var timezone: String,
    var retries: Int,
    var source: String,
    var expiresAt: Int,
    var status: AppointmentAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentAdjustmentStatus): Boolean {
        when (status) {
            AppointmentAdjustmentStatus.NEW -> {
                when (next) {
                    AppointmentAdjustmentStatus.RUNNING, AppointmentAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            AppointmentAdjustmentStatus.QUEUED -> return next == AppointmentAdjustmentStatus.DONE || next == AppointmentAdjustmentStatus.FAILED
            AppointmentAdjustmentStatus.RUNNING -> return next == AppointmentAdjustmentStatus.FAILED
            AppointmentAdjustmentStatus.DONE -> return next == AppointmentAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AppointmentAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("appointment adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"appointment adjustment"} $code"

    fun weightedValue(): Double = round(expiresAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): AppointmentAdjustment {
        return AppointmentAdjustment(
            code = code,
            label = label,
            name = name,
            timezone = timezone,
            retries = retries,
            source = source,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a appointment adjustment in status new. */
fun newAppointmentAdjustment(code: String, label: String): AppointmentAdjustment {
    return AppointmentAdjustment(
        code = code,
        label = label,
        name = "",
        timezone = "",
        retries = 0,
        source = "",
        expiresAt = 0,
        status = AppointmentAdjustmentStatus.NEW,
    )
}
