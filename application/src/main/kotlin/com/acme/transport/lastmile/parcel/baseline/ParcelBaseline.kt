package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.text.normalize

// The parcel baseline records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel baseline is in its life cycle. */
enum class ParcelBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A parcel baseline as the parcel team keeps it. */
data class ParcelBaseline(
    var code: String,
    var label: String,
    var channel: String,
    var category: String,
    var routeCode: String,
    var score: Double,
    var status: ParcelBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelBaselineStatus): Boolean {
        when (status) {
            ParcelBaselineStatus.PLANNED -> return next == ParcelBaselineStatus.CANCELLED
            ParcelBaselineStatus.SCHEDULED -> return next == ParcelBaselineStatus.COMPLETED
            ParcelBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    ParcelBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            ParcelBaselineStatus.COMPLETED -> return next == ParcelBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ParcelBaselineStatus) {
        if (!canMoveTo(next)) {
            error("parcel baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): ParcelBaseline {
        return ParcelBaseline(
            code = code,
            label = label,
            channel = channel,
            category = category,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a parcel baseline in status planned. */
fun newParcelBaseline(code: String, label: String): ParcelBaseline {
    return ParcelBaseline(
        code = code,
        label = label,
        channel = "",
        category = "",
        routeCode = "",
        score = 0.0,
        status = ParcelBaselineStatus.PLANNED,
    )
}
