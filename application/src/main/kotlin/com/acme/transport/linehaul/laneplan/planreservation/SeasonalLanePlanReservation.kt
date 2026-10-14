package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal lane plan reservation records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a seasonal lane plan reservation is in its life cycle. */
enum class SeasonalLanePlanReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal lane plan reservation and its current status. */
data class SeasonalLanePlanReservation(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var cost: Double,
    var etaMinutes: Int,
    var amount: Double,
    var status: SeasonalLanePlanReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLanePlanReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLanePlanReservationStatus): Boolean {
        when (status) {
            SeasonalLanePlanReservationStatus.OPEN -> {
                when (next) {
                    SeasonalLanePlanReservationStatus.IN_REVIEW, SeasonalLanePlanReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalLanePlanReservationStatus.IN_REVIEW -> return next == SeasonalLanePlanReservationStatus.RESOLVED || next == SeasonalLanePlanReservationStatus.REOPENED
            SeasonalLanePlanReservationStatus.RESOLVED -> {
                when (next) {
                    SeasonalLanePlanReservationStatus.REOPENED, SeasonalLanePlanReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalLanePlanReservationStatus.REOPENED -> return next == SeasonalLanePlanReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalLanePlanReservationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal lane plan reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((distanceKm + amount) * 100) / 100

    fun snapshot(): SeasonalLanePlanReservation {
        return SeasonalLanePlanReservation(
            code = code,
            title = title,
            distanceKm = distanceKm,
            cost = cost,
            etaMinutes = etaMinutes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal lane plan reservation in status open. */
fun newSeasonalLanePlanReservation(code: String, title: String): SeasonalLanePlanReservation {
    return SeasonalLanePlanReservation(
        code = code,
        title = title,
        distanceKm = 0.0,
        cost = 0.0,
        etaMinutes = 0,
        amount = 0.0,
        status = SeasonalLanePlanReservationStatus.OPEN,
    )
}
