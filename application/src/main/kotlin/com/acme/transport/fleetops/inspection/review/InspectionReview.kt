package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection review records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection review is in its life cycle. */
enum class InspectionReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection review. Its code is unique within a tenant. */
data class InspectionReview(
    var code: String,
    var title: String,
    var shiftCode: String,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var windowDays: Int,
    var rate: Double,
    var checksum: String,
    var carrier: String,
    var vehicleId: String,
    var maximum: Double,
    var status: InspectionReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionReviewStatus): Boolean {
        when (status) {
            InspectionReviewStatus.PLANNED -> {
                when (next) {
                    InspectionReviewStatus.IN_PROGRESS, InspectionReviewStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionReviewStatus.SCHEDULED -> {
                when (next) {
                    InspectionReviewStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionReviewStatus.IN_PROGRESS -> return next == InspectionReviewStatus.COMPLETED || next == InspectionReviewStatus.CANCELLED
            InspectionReviewStatus.COMPLETED -> {
                when (next) {
                    InspectionReviewStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionReviewStatus) {
        if (!canMoveTo(next)) {
            error("inspection review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round(rate * fuelLiters * 100) / 100

    fun snapshot(): InspectionReview {
        return InspectionReview(
            code = code,
            title = title,
            shiftCode = shiftCode,
            fuelLiters = fuelLiters,
            tags = tags,
            windowDays = windowDays,
            rate = rate,
            checksum = checksum,
            carrier = carrier,
            vehicleId = vehicleId,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection review in status planned. */
fun newInspectionReview(code: String, title: String): InspectionReview {
    return InspectionReview(
        code = code,
        title = title,
        shiftCode = "",
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        windowDays = 0,
        rate = 0.0,
        checksum = "",
        carrier = "",
        vehicleId = "",
        maximum = 0.0,
        status = InspectionReviewStatus.PLANNED,
    )
}
