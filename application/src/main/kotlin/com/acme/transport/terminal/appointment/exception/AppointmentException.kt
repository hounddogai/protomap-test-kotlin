package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment exception records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment exception is in its life cycle. */
enum class AppointmentExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A appointment exception as the appointment team keeps it. */
data class AppointmentException(
    var code: String,
    var title: String,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var retries: Int,
    var version: Int,
    var ratio: Double,
    var threshold: Double,
    var expiresAt: Int,
    var status: AppointmentExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentExceptionStatus): Boolean {
        when (status) {
            AppointmentExceptionStatus.CREATED -> {
                when (next) {
                    AppointmentExceptionStatus.VALIDATED, AppointmentExceptionStatus.PUBLISHED -> return true
                }
                return false
            }
            AppointmentExceptionStatus.VALIDATED -> return next == AppointmentExceptionStatus.PUBLISHED
            AppointmentExceptionStatus.PUBLISHED -> {
                when (next) {
                    AppointmentExceptionStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentExceptionStatus) {
        if (!canMoveTo(next)) {
            error("appointment exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((ratio + version.toDouble()) * 100) / 100

    fun snapshot(): AppointmentException {
        return AppointmentException(
            code = code,
            title = title,
            odometer = odometer,
            attributes = attributes,
            rate = rate,
            retries = retries,
            version = version,
            ratio = ratio,
            threshold = threshold,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment exception in status created. */
fun newAppointmentException(code: String, title: String): AppointmentException {
    return AppointmentException(
        code = code,
        title = title,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        retries = 0,
        version = 0,
        ratio = 0.0,
        threshold = 0.0,
        expiresAt = 0,
        status = AppointmentExceptionStatus.CREATED,
    )
}
