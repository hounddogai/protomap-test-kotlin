package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient redelivery estimate is in its life cycle. */
enum class LenientRedeliveryEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient redelivery estimate and its current status. */
data class LenientRedeliveryEstimate(
    var code: String,
    var title: String,
    var capacity: Int,
    var budget: Double,
    var depot: String,
    var routeCode: String,
    var weight: Double,
    var status: LenientRedeliveryEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRedeliveryEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRedeliveryEstimateStatus): Boolean {
        when (status) {
            LenientRedeliveryEstimateStatus.REQUESTED -> {
                when (next) {
                    LenientRedeliveryEstimateStatus.REVOKED, LenientRedeliveryEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            LenientRedeliveryEstimateStatus.GRANTED -> {
                when (next) {
                    LenientRedeliveryEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            LenientRedeliveryEstimateStatus.REVOKED -> return next == LenientRedeliveryEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientRedeliveryEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient redelivery estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((weight + capacity.toDouble()) * 100) / 100

    fun snapshot(): LenientRedeliveryEstimate {
        return LenientRedeliveryEstimate(
            code = code,
            title = title,
            capacity = capacity,
            budget = budget,
            depot = depot,
            routeCode = routeCode,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient redelivery estimate in status requested. */
fun newLenientRedeliveryEstimate(code: String, title: String): LenientRedeliveryEstimate {
    return LenientRedeliveryEstimate(
        code = code,
        title = title,
        capacity = 0,
        budget = 0.0,
        depot = "",
        routeCode = "",
        weight = 0.0,
        status = LenientRedeliveryEstimateStatus.REQUESTED,
    )
}
