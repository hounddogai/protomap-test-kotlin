package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient appointment metric is in its life cycle. */
enum class LenientAppointmentMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient appointment metric and its current status. */
data class LenientAppointmentMetric(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var pallets: Int,
    var batchSize: Int,
    var sequence: Int,
    var status: LenientAppointmentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAppointmentMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAppointmentMetricStatus): Boolean {
        when (status) {
            LenientAppointmentMetricStatus.IDLE -> return next == LenientAppointmentMetricStatus.WARMING || next == LenientAppointmentMetricStatus.STOPPED
            LenientAppointmentMetricStatus.WARMING -> {
                when (next) {
                    LenientAppointmentMetricStatus.DRAINING, LenientAppointmentMetricStatus.STOPPED -> return true
                }
                return false
            }
            LenientAppointmentMetricStatus.READY -> return next == LenientAppointmentMetricStatus.DRAINING || next == LenientAppointmentMetricStatus.STOPPED
            LenientAppointmentMetricStatus.DRAINING -> {
                when (next) {
                    LenientAppointmentMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAppointmentMetricStatus) {
        if (!canMoveTo(next)) {
            error("lenient appointment metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient appointment metric"} $code"

    fun weightedLoad(): Double = round(sequence.toDouble() * distanceKm * 100) / 100

    fun snapshot(): LenientAppointmentMetric {
        return LenientAppointmentMetric(
            code = code,
            label = label,
            distanceKm = distanceKm,
            pallets = pallets,
            batchSize = batchSize,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient appointment metric in status idle. */
fun newLenientAppointmentMetric(code: String, label: String): LenientAppointmentMetric {
    return LenientAppointmentMetric(
        code = code,
        label = label,
        distanceKm = 0.0,
        pallets = 0,
        batchSize = 0,
        sequence = 0,
        status = LenientAppointmentMetricStatus.IDLE,
    )
}
