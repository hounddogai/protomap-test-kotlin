package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered appointment claim is in its life cycle. */
enum class TieredAppointmentClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered appointment claim and its current status. */
data class TieredAppointmentClaim(
    var code: String,
    var title: String,
    var label: String,
    var durationMinutes: Int,
    var version: Int,
    var ratio: Double,
    var category: String,
    var reviewer: String,
    var status: TieredAppointmentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAppointmentClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAppointmentClaimStatus): Boolean {
        when (status) {
            TieredAppointmentClaimStatus.PLANNED -> return next == TieredAppointmentClaimStatus.SCHEDULED || next == TieredAppointmentClaimStatus.COMPLETED
            TieredAppointmentClaimStatus.SCHEDULED -> {
                when (next) {
                    TieredAppointmentClaimStatus.COMPLETED -> return true
                }
                return false
            }
            TieredAppointmentClaimStatus.IN_PROGRESS -> return next == TieredAppointmentClaimStatus.COMPLETED
            TieredAppointmentClaimStatus.COMPLETED -> return next == TieredAppointmentClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredAppointmentClaimStatus) {
        if (!canMoveTo(next)) {
            error("tiered appointment claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"tiered appointment claim"} $code"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): TieredAppointmentClaim {
        return TieredAppointmentClaim(
            code = code,
            title = title,
            label = label,
            durationMinutes = durationMinutes,
            version = version,
            ratio = ratio,
            category = category,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered appointment claim in status planned. */
fun newTieredAppointmentClaim(code: String, title: String): TieredAppointmentClaim {
    return TieredAppointmentClaim(
        code = code,
        title = title,
        label = "",
        durationMinutes = 0,
        version = 0,
        ratio = 0.0,
        category = "",
        reviewer = "",
        status = TieredAppointmentClaimStatus.PLANNED,
    )
}
