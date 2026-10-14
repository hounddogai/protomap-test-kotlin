package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lane plan reservation is in its life cycle. */
enum class ScheduledLanePlanReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled lane plan reservation as the lane plan team keeps it. */
data class ScheduledLanePlanReservation(
    var code: String,
    var label: String,
    var ratio: Double,
    var score: Double,
    var pallets: Int,
    var status: ScheduledLanePlanReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanReservationStatus): Boolean {
        when (status) {
            ScheduledLanePlanReservationStatus.DRAFT -> {
                when (next) {
                    ScheduledLanePlanReservationStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledLanePlanReservationStatus.PENDING -> {
                when (next) {
                    ScheduledLanePlanReservationStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledLanePlanReservationStatus.ACTIVE -> return next == ScheduledLanePlanReservationStatus.SUSPENDED
            ScheduledLanePlanReservationStatus.SUSPENDED -> return next == ScheduledLanePlanReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanReservationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round(pallets.toDouble() * ratio * 100) / 100

    fun snapshot(): ScheduledLanePlanReservation {
        return ScheduledLanePlanReservation(
            code = code,
            label = label,
            ratio = ratio,
            score = score,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled lane plan reservation in status draft. */
fun newScheduledLanePlanReservation(code: String, label: String): ScheduledLanePlanReservation {
    return ScheduledLanePlanReservation(
        code = code,
        label = label,
        ratio = 0.0,
        score = 0.0,
        pallets = 0,
        status = ScheduledLanePlanReservationStatus.DRAFT,
    )
}
