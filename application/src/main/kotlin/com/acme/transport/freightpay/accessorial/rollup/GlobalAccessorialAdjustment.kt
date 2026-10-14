package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.text.normalize

/** Where a global accessorial adjustment is in its life cycle. */
enum class GlobalAccessorialAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global accessorial adjustment, with what the planners need to act on it. */
data class GlobalAccessorialAdjustment(
    var code: String,
    var label: String,
    var carrier: String,
    var archived: Boolean,
    var distanceKm: Double,
    var status: GlobalAccessorialAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAccessorialAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAccessorialAdjustmentStatus): Boolean {
        when (status) {
            GlobalAccessorialAdjustmentStatus.REQUESTED -> return next == GlobalAccessorialAdjustmentStatus.REVOKED
            GlobalAccessorialAdjustmentStatus.GRANTED -> {
                when (next) {
                    GlobalAccessorialAdjustmentStatus.REVOKED, GlobalAccessorialAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalAccessorialAdjustmentStatus.REVOKED -> {
                when (next) {
                    GlobalAccessorialAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAccessorialAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global accessorial adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global accessorial adjustment"} $code"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): GlobalAccessorialAdjustment {
        return GlobalAccessorialAdjustment(
            code = code,
            label = label,
            carrier = carrier,
            archived = archived,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global accessorial adjustment in status requested. */
fun newGlobalAccessorialAdjustment(code: String, label: String): GlobalAccessorialAdjustment {
    return GlobalAccessorialAdjustment(
        code = code,
        label = label,
        carrier = "",
        archived = false,
        distanceKm = 0.0,
        status = GlobalAccessorialAdjustmentStatus.REQUESTED,
    )
}
