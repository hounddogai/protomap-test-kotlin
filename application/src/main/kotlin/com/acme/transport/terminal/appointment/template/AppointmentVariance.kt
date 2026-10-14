package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment variance is in its life cycle. */
enum class AppointmentVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A appointment variance as the appointment team keeps it. */
data class AppointmentVariance(
    var code: String,
    var title: String,
    var locale: String,
    var floorValue: Double,
    var score: Double,
    var segment: String,
    var distanceKm: Double,
    var active: Boolean,
    var name: String,
    var status: AppointmentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentVarianceStatus): Boolean {
        when (status) {
            AppointmentVarianceStatus.NEW -> {
                when (next) {
                    AppointmentVarianceStatus.QUEUED, AppointmentVarianceStatus.DONE -> return true
                }
                return false
            }
            AppointmentVarianceStatus.QUEUED -> return next == AppointmentVarianceStatus.DONE
            AppointmentVarianceStatus.RUNNING -> {
                when (next) {
                    AppointmentVarianceStatus.DONE -> return true
                }
                return false
            }
            AppointmentVarianceStatus.DONE -> return next == AppointmentVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AppointmentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("appointment variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment variance"} $code"

    fun netVolume(): Double = round((floorValue + score) * 100) / 100

    fun snapshot(): AppointmentVariance {
        return AppointmentVariance(
            code = code,
            title = title,
            locale = locale,
            floorValue = floorValue,
            score = score,
            segment = segment,
            distanceKm = distanceKm,
            active = active,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a appointment variance in status new. */
fun newAppointmentVariance(code: String, title: String): AppointmentVariance {
    return AppointmentVariance(
        code = code,
        title = title,
        locale = "",
        floorValue = 0.0,
        score = 0.0,
        segment = "",
        distanceKm = 0.0,
        active = false,
        name = "",
        status = AppointmentVarianceStatus.NEW,
    )
}
