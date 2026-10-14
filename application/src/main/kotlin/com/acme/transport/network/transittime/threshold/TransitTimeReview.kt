package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time review is in its life cycle. */
enum class TransitTimeReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A transit time review as the transit time team keeps it. */
data class TransitTimeReview(
    var code: String,
    var label: String,
    var checksum: String,
    var name: String,
    var unit: String,
    var margin: Double,
    var ratio: Double,
    var pallets: Int,
    var fuelLiters: Double,
    var carrier: String,
    var driverId: String,
    var status: TransitTimeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeReviewStatus): Boolean {
        when (status) {
            TransitTimeReviewStatus.IDLE -> {
                when (next) {
                    TransitTimeReviewStatus.READY, TransitTimeReviewStatus.STOPPED -> return true
                }
                return false
            }
            TransitTimeReviewStatus.WARMING -> {
                when (next) {
                    TransitTimeReviewStatus.READY, TransitTimeReviewStatus.DRAINING -> return true
                }
                return false
            }
            TransitTimeReviewStatus.READY -> {
                when (next) {
                    TransitTimeReviewStatus.DRAINING, TransitTimeReviewStatus.STOPPED -> return true
                }
                return false
            }
            TransitTimeReviewStatus.DRAINING -> return next == TransitTimeReviewStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TransitTimeReviewStatus) {
        if (!canMoveTo(next)) {
            error("transit time review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round(ratio * pallets.toDouble() * 100) / 100

    fun snapshot(): TransitTimeReview {
        return TransitTimeReview(
            code = code,
            label = label,
            checksum = checksum,
            name = name,
            unit = unit,
            margin = margin,
            ratio = ratio,
            pallets = pallets,
            fuelLiters = fuelLiters,
            carrier = carrier,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a transit time review in status idle. */
fun newTransitTimeReview(code: String, label: String): TransitTimeReview {
    return TransitTimeReview(
        code = code,
        label = label,
        checksum = "",
        name = "",
        unit = "",
        margin = 0.0,
        ratio = 0.0,
        pallets = 0,
        fuelLiters = 0.0,
        carrier = "",
        driverId = "",
        status = TransitTimeReviewStatus.IDLE,
    )
}
