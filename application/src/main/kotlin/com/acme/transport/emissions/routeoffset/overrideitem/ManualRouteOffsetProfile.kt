package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual route offset profile is in its life cycle. */
enum class ManualRouteOffsetProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual route offset profile and its current status. */
data class ManualRouteOffsetProfile(
    var code: String,
    var name: String,
    var externalRef: String,
    var currency: String,
    var odometer: Int,
    var quantity: Int,
    var depot: String,
    var status: ManualRouteOffsetProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRouteOffsetProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRouteOffsetProfileStatus): Boolean {
        when (status) {
            ManualRouteOffsetProfileStatus.PLANNED -> {
                when (next) {
                    ManualRouteOffsetProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualRouteOffsetProfileStatus.SCHEDULED -> return next == ManualRouteOffsetProfileStatus.IN_PROGRESS
            ManualRouteOffsetProfileStatus.IN_PROGRESS -> {
                when (next) {
                    ManualRouteOffsetProfileStatus.COMPLETED, ManualRouteOffsetProfileStatus.CANCELLED -> return true
                }
                return false
            }
            ManualRouteOffsetProfileStatus.COMPLETED -> {
                when (next) {
                    ManualRouteOffsetProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRouteOffsetProfileStatus) {
        if (!canMoveTo(next)) {
            error("manual route offset profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((odometer.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): ManualRouteOffsetProfile {
        return ManualRouteOffsetProfile(
            code = code,
            name = name,
            externalRef = externalRef,
            currency = currency,
            odometer = odometer,
            quantity = quantity,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual route offset profile in status planned. */
fun newManualRouteOffsetProfile(code: String, name: String): ManualRouteOffsetProfile {
    return ManualRouteOffsetProfile(
        code = code,
        name = name,
        externalRef = "",
        currency = "",
        odometer = 0,
        quantity = 0,
        depot = "",
        status = ManualRouteOffsetProfileStatus.PLANNED,
    )
}
