package com.acme.transport.customs.inspection.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection reservation is in its life cycle. */
enum class RollingInspectionReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling inspection reservation and its current status. */
data class RollingInspectionReservation(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var sequence: Int,
    var retries: Int,
    var category: String,
    var score: Double,
    var quantity: Int,
    var version: Int,
    var status: RollingInspectionReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionReservationStatus): Boolean {
        when (status) {
            RollingInspectionReservationStatus.PROPOSED -> {
                when (next) {
                    RollingInspectionReservationStatus.REJECTED, RollingInspectionReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingInspectionReservationStatus.APPROVED -> {
                when (next) {
                    RollingInspectionReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingInspectionReservationStatus.REJECTED -> {
                when (next) {
                    RollingInspectionReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingInspectionReservationStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round(score * version.toDouble() * 100) / 100

    fun snapshot(): RollingInspectionReservation {
        return RollingInspectionReservation(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            sequence = sequence,
            retries = retries,
            category = category,
            score = score,
            quantity = quantity,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling inspection reservation in status proposed. */
fun newRollingInspectionReservation(code: String, name: String): RollingInspectionReservation {
    return RollingInspectionReservation(
        code = code,
        name = name,
        fuelLiters = 0.0,
        sequence = 0,
        retries = 0,
        category = "",
        score = 0.0,
        quantity = 0,
        version = 0,
        status = RollingInspectionReservationStatus.PROPOSED,
    )
}
