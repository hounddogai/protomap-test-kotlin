package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling estimate is in its life cycle. */
enum class IdlingEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A idling estimate as the idling team keeps it. */
data class IdlingEstimate(
    var code: String,
    var name: String,
    var cost: Double,
    var unit: String,
    var odometer: Int,
    var status: IdlingEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingEstimateStatus): Boolean {
        when (status) {
            IdlingEstimateStatus.OPEN -> {
                when (next) {
                    IdlingEstimateStatus.RESOLVED -> return true
                }
                return false
            }
            IdlingEstimateStatus.IN_REVIEW -> {
                when (next) {
                    IdlingEstimateStatus.RESOLVED -> return true
                }
                return false
            }
            IdlingEstimateStatus.RESOLVED -> return next == IdlingEstimateStatus.REOPENED
            IdlingEstimateStatus.REOPENED -> return next == IdlingEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: IdlingEstimateStatus) {
        if (!canMoveTo(next)) {
            error("idling estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((cost - odometer.toDouble()) * 100) / 100

    fun snapshot(): IdlingEstimate {
        return IdlingEstimate(
            code = code,
            name = name,
            cost = cost,
            unit = unit,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a idling estimate in status open. */
fun newIdlingEstimate(code: String, name: String): IdlingEstimate {
    return IdlingEstimate(
        code = code,
        name = name,
        cost = 0.0,
        unit = "",
        odometer = 0,
        status = IdlingEstimateStatus.OPEN,
    )
}
