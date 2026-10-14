package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback appointment baseline is in its life cycle. */
enum class FallbackAppointmentBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback appointment baseline. Its code is unique within a tenant. */
data class FallbackAppointmentBaseline(
    var code: String,
    var title: String,
    var endsAt: Int,
    var pinned: Boolean,
    var etaMinutes: Int,
    var maximum: Double,
    var status: FallbackAppointmentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAppointmentBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAppointmentBaselineStatus): Boolean {
        when (status) {
            FallbackAppointmentBaselineStatus.IDLE -> {
                when (next) {
                    FallbackAppointmentBaselineStatus.STOPPED -> return true
                }
                return false
            }
            FallbackAppointmentBaselineStatus.WARMING -> {
                when (next) {
                    FallbackAppointmentBaselineStatus.READY -> return true
                }
                return false
            }
            FallbackAppointmentBaselineStatus.READY -> return next == FallbackAppointmentBaselineStatus.DRAINING || next == FallbackAppointmentBaselineStatus.STOPPED
            FallbackAppointmentBaselineStatus.DRAINING -> {
                when (next) {
                    FallbackAppointmentBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAppointmentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fallback appointment baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((endsAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): FallbackAppointmentBaseline {
        return FallbackAppointmentBaseline(
            code = code,
            title = title,
            endsAt = endsAt,
            pinned = pinned,
            etaMinutes = etaMinutes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback appointment baseline in status idle. */
fun newFallbackAppointmentBaseline(code: String, title: String): FallbackAppointmentBaseline {
    return FallbackAppointmentBaseline(
        code = code,
        title = title,
        endsAt = 0,
        pinned = false,
        etaMinutes = 0,
        maximum = 0.0,
        status = FallbackAppointmentBaselineStatus.IDLE,
    )
}
