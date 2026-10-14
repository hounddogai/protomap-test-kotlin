package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment metric is in its life cycle. */
enum class AppointmentMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a appointment metric and its current status. */
data class AppointmentMetric(
    var code: String,
    var label: String,
    var sequence: Int,
    var pallets: Int,
    var notes: String,
    var currency: String,
    var revision: Int,
    var createdAt: Int,
    var category: String,
    var version: Int,
    var status: AppointmentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentMetricStatus): Boolean {
        when (status) {
            AppointmentMetricStatus.NEW -> {
                when (next) {
                    AppointmentMetricStatus.QUEUED, AppointmentMetricStatus.DONE -> return true
                }
                return false
            }
            AppointmentMetricStatus.QUEUED -> return next == AppointmentMetricStatus.RUNNING || next == AppointmentMetricStatus.FAILED
            AppointmentMetricStatus.RUNNING -> {
                when (next) {
                    AppointmentMetricStatus.DONE, AppointmentMetricStatus.FAILED -> return true
                }
                return false
            }
            AppointmentMetricStatus.DONE -> return next == AppointmentMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AppointmentMetricStatus) {
        if (!canMoveTo(next)) {
            error("appointment metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((pallets.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): AppointmentMetric {
        return AppointmentMetric(
            code = code,
            label = label,
            sequence = sequence,
            pallets = pallets,
            notes = notes,
            currency = currency,
            revision = revision,
            createdAt = createdAt,
            category = category,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a appointment metric in status new. */
fun newAppointmentMetric(code: String, label: String): AppointmentMetric {
    return AppointmentMetric(
        code = code,
        label = label,
        sequence = 0,
        pallets = 0,
        notes = "",
        currency = "",
        revision = 0,
        createdAt = 0,
        category = "",
        version = 0,
        status = AppointmentMetricStatus.NEW,
    )
}
