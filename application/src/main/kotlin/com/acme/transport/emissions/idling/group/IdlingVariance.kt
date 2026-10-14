package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling variance is in its life cycle. */
enum class IdlingVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a idling variance and its current status. */
data class IdlingVariance(
    var code: String,
    var name: String,
    var cost: Double,
    var odometer: Int,
    var summary: String,
    var label: String,
    var distanceKm: Double,
    var ownerId: String,
    var shiftCode: String,
    var pallets: Int,
    var status: IdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingVarianceStatus): Boolean {
        when (status) {
            IdlingVarianceStatus.OPEN -> return next == IdlingVarianceStatus.IN_REVIEW
            IdlingVarianceStatus.IN_REVIEW -> {
                when (next) {
                    IdlingVarianceStatus.REOPENED -> return true
                }
                return false
            }
            IdlingVarianceStatus.RESOLVED -> return next == IdlingVarianceStatus.REOPENED || next == IdlingVarianceStatus.ARCHIVED
            IdlingVarianceStatus.REOPENED -> {
                when (next) {
                    IdlingVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("idling variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"idling variance"} $code"

    fun netExposure(): Double = round(cost * odometer.toDouble() * 100) / 100

    fun snapshot(): IdlingVariance {
        return IdlingVariance(
            code = code,
            name = name,
            cost = cost,
            odometer = odometer,
            summary = summary,
            label = label,
            distanceKm = distanceKm,
            ownerId = ownerId,
            shiftCode = shiftCode,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a idling variance in status open. */
fun newIdlingVariance(code: String, name: String): IdlingVariance {
    return IdlingVariance(
        code = code,
        name = name,
        cost = 0.0,
        odometer = 0,
        summary = "",
        label = "",
        distanceKm = 0.0,
        ownerId = "",
        shiftCode = "",
        pallets = 0,
        status = IdlingVarianceStatus.OPEN,
    )
}
