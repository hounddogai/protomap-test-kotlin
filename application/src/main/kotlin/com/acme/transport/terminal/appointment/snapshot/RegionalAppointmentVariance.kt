package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional appointment variance is in its life cycle. */
enum class RegionalAppointmentVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional appointment variance. Its code is unique within a tenant. */
data class RegionalAppointmentVariance(
    var code: String,
    var name: String,
    var visible: Boolean,
    var loadKg: Double,
    var cost: Double,
    var unit: String,
    var carrier: String,
    var locale: String,
    var startsAt: Int,
    var externalRef: String,
    var tags: MutableList<String>,
    var status: RegionalAppointmentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAppointmentVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAppointmentVarianceStatus): Boolean {
        when (status) {
            RegionalAppointmentVarianceStatus.NEW -> return next == RegionalAppointmentVarianceStatus.RUNNING || next == RegionalAppointmentVarianceStatus.FAILED
            RegionalAppointmentVarianceStatus.QUEUED -> return next == RegionalAppointmentVarianceStatus.RUNNING || next == RegionalAppointmentVarianceStatus.DONE
            RegionalAppointmentVarianceStatus.RUNNING -> return next == RegionalAppointmentVarianceStatus.DONE
            RegionalAppointmentVarianceStatus.DONE -> return next == RegionalAppointmentVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalAppointmentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("regional appointment variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((cost + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalAppointmentVariance {
        return RegionalAppointmentVariance(
            code = code,
            name = name,
            visible = visible,
            loadKg = loadKg,
            cost = cost,
            unit = unit,
            carrier = carrier,
            locale = locale,
            startsAt = startsAt,
            externalRef = externalRef,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional appointment variance in status new. */
fun newRegionalAppointmentVariance(code: String, name: String): RegionalAppointmentVariance {
    return RegionalAppointmentVariance(
        code = code,
        name = name,
        visible = false,
        loadKg = 0.0,
        cost = 0.0,
        unit = "",
        carrier = "",
        locale = "",
        startsAt = 0,
        externalRef = "",
        tags = mutableListOf<String>(),
        status = RegionalAppointmentVarianceStatus.NEW,
    )
}
