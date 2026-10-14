package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection review is in its life cycle. */
enum class RollingInspectionReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling inspection review as the inspection team keeps it. */
data class RollingInspectionReview(
    var code: String,
    var name: String,
    var odometer: Int,
    var pallets: Int,
    var visible: Boolean,
    var tags: MutableList<String>,
    var threshold: Double,
    var shiftCode: String,
    var contactEmail: String,
    var stopCount: Int,
    var minimum: Double,
    var status: RollingInspectionReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionReviewStatus): Boolean {
        when (status) {
            RollingInspectionReviewStatus.REQUESTED -> {
                when (next) {
                    RollingInspectionReviewStatus.REVOKED, RollingInspectionReviewStatus.EXPIRED -> return true
                }
                return false
            }
            RollingInspectionReviewStatus.GRANTED -> {
                when (next) {
                    RollingInspectionReviewStatus.REVOKED, RollingInspectionReviewStatus.EXPIRED -> return true
                }
                return false
            }
            RollingInspectionReviewStatus.REVOKED -> return next == RollingInspectionReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingInspectionReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round(stopCount.toDouble() * threshold * 100) / 100

    fun snapshot(): RollingInspectionReview {
        return RollingInspectionReview(
            code = code,
            name = name,
            odometer = odometer,
            pallets = pallets,
            visible = visible,
            tags = tags,
            threshold = threshold,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            stopCount = stopCount,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling inspection review in status requested. */
fun newRollingInspectionReview(code: String, name: String): RollingInspectionReview {
    return RollingInspectionReview(
        code = code,
        name = name,
        odometer = 0,
        pallets = 0,
        visible = false,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        shiftCode = "",
        contactEmail = "",
        stopCount = 0,
        minimum = 0.0,
        status = RollingInspectionReviewStatus.REQUESTED,
    )
}
