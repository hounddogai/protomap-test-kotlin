package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan reservation records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a lane plan reservation is in its life cycle. */
enum class LanePlanReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lane plan reservation as the lane plan team keeps it. */
data class LanePlanReservation(
    var code: String,
    var title: String,
    var checksum: String,
    var discount: Double,
    var shiftCode: String,
    var approved: Boolean,
    var rate: Double,
    var reviewer: String,
    var maximum: Double,
    var margin: Double,
    var status: LanePlanReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanReservationStatus): Boolean {
        when (status) {
            LanePlanReservationStatus.PROPOSED -> return next == LanePlanReservationStatus.REJECTED || next == LanePlanReservationStatus.WITHDRAWN
            LanePlanReservationStatus.APPROVED -> return next == LanePlanReservationStatus.REJECTED
            LanePlanReservationStatus.REJECTED -> {
                when (next) {
                    LanePlanReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanReservationStatus) {
        if (!canMoveTo(next)) {
            error("lane plan reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round(discount * rate * 100) / 100

    fun snapshot(): LanePlanReservation {
        return LanePlanReservation(
            code = code,
            title = title,
            checksum = checksum,
            discount = discount,
            shiftCode = shiftCode,
            approved = approved,
            rate = rate,
            reviewer = reviewer,
            maximum = maximum,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan reservation in status proposed. */
fun newLanePlanReservation(code: String, title: String): LanePlanReservation {
    return LanePlanReservation(
        code = code,
        title = title,
        checksum = "",
        discount = 0.0,
        shiftCode = "",
        approved = false,
        rate = 0.0,
        reviewer = "",
        maximum = 0.0,
        margin = 0.0,
        status = LanePlanReservationStatus.PROPOSED,
    )
}
