package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment baseline is in its life cycle. */
enum class AppointmentBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One appointment baseline, with what the planners need to act on it. */
data class AppointmentBaseline(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var ratio: Double,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var floorValue: Double,
    var version: Int,
    var country: String,
    var priority: Int,
    var status: AppointmentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentBaselineStatus): Boolean {
        when (status) {
            AppointmentBaselineStatus.NEW -> return next == AppointmentBaselineStatus.QUEUED
            AppointmentBaselineStatus.QUEUED -> return next == AppointmentBaselineStatus.RUNNING
            AppointmentBaselineStatus.RUNNING -> return next == AppointmentBaselineStatus.DONE || next == AppointmentBaselineStatus.FAILED
            AppointmentBaselineStatus.DONE -> return next == AppointmentBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AppointmentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("appointment baseline $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"appointment baseline"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round(fuelLiters * ceiling * 100) / 100

    fun snapshot(): AppointmentBaseline {
        return AppointmentBaseline(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            ratio = ratio,
            attributes = attributes,
            ceiling = ceiling,
            floorValue = floorValue,
            version = version,
            country = country,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a appointment baseline in status new. */
fun newAppointmentBaseline(code: String, label: String): AppointmentBaseline {
    return AppointmentBaseline(
        code = code,
        label = label,
        fuelLiters = 0.0,
        ratio = 0.0,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        floorValue = 0.0,
        version = 0,
        country = "",
        priority = 0,
        status = AppointmentBaselineStatus.NEW,
    )
}
