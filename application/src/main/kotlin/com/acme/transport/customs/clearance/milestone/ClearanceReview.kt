package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance review is in its life cycle. */
enum class ClearanceReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance review as the clearance team keeps it. */
data class ClearanceReview(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var odometer: Int,
    var floorValue: Double,
    var ownerId: String,
    var source: String,
    var version: Int,
    var currency: String,
    var retries: Int,
    var status: ClearanceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceReviewStatus): Boolean {
        when (status) {
            ClearanceReviewStatus.IDLE -> return next == ClearanceReviewStatus.READY || next == ClearanceReviewStatus.DRAINING
            ClearanceReviewStatus.WARMING -> return next == ClearanceReviewStatus.DRAINING || next == ClearanceReviewStatus.STOPPED
            ClearanceReviewStatus.READY -> {
                when (next) {
                    ClearanceReviewStatus.DRAINING, ClearanceReviewStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceReviewStatus.DRAINING -> {
                when (next) {
                    ClearanceReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceReviewStatus) {
        if (!canMoveTo(next)) {
            error("clearance review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round(retries.toDouble() * distanceKm * 100) / 100

    fun snapshot(): ClearanceReview {
        return ClearanceReview(
            code = code,
            title = title,
            distanceKm = distanceKm,
            odometer = odometer,
            floorValue = floorValue,
            ownerId = ownerId,
            source = source,
            version = version,
            currency = currency,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a clearance review in status idle. */
fun newClearanceReview(code: String, title: String): ClearanceReview {
    return ClearanceReview(
        code = code,
        title = title,
        distanceKm = 0.0,
        odometer = 0,
        floorValue = 0.0,
        ownerId = "",
        source = "",
        version = 0,
        currency = "",
        retries = 0,
        status = ClearanceReviewStatus.IDLE,
    )
}
