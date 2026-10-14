package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient appointment audit is in its life cycle. */
enum class LenientAppointmentAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lenient appointment audit, with what the planners need to act on it. */
data class LenientAppointmentAudit(
    var code: String,
    var label: String,
    var priority: Int,
    var floorValue: Double,
    var channel: String,
    var threshold: Double,
    var cost: Double,
    var stopCount: Int,
    var title: String,
    var margin: Double,
    var notes: String,
    var status: LenientAppointmentAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAppointmentAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAppointmentAuditStatus): Boolean {
        when (status) {
            LenientAppointmentAuditStatus.NEW -> {
                when (next) {
                    LenientAppointmentAuditStatus.DONE, LenientAppointmentAuditStatus.FAILED -> return true
                }
                return false
            }
            LenientAppointmentAuditStatus.QUEUED -> return next == LenientAppointmentAuditStatus.DONE || next == LenientAppointmentAuditStatus.FAILED
            LenientAppointmentAuditStatus.RUNNING -> return next == LenientAppointmentAuditStatus.FAILED
            LenientAppointmentAuditStatus.DONE -> {
                when (next) {
                    LenientAppointmentAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAppointmentAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient appointment audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round(cost * stopCount.toDouble() * 100) / 100

    fun snapshot(): LenientAppointmentAudit {
        return LenientAppointmentAudit(
            code = code,
            label = label,
            priority = priority,
            floorValue = floorValue,
            channel = channel,
            threshold = threshold,
            cost = cost,
            stopCount = stopCount,
            title = title,
            margin = margin,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient appointment audit in status new. */
fun newLenientAppointmentAudit(code: String, label: String): LenientAppointmentAudit {
    return LenientAppointmentAudit(
        code = code,
        label = label,
        priority = 0,
        floorValue = 0.0,
        channel = "",
        threshold = 0.0,
        cost = 0.0,
        stopCount = 0,
        title = "",
        margin = 0.0,
        notes = "",
        status = LenientAppointmentAuditStatus.NEW,
    )
}
