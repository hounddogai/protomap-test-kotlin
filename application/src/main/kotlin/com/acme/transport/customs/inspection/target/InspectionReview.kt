package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection review is in its life cycle. */
enum class InspectionReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A inspection review as the inspection team keeps it. */
data class InspectionReview(
    var code: String,
    var name: String,
    var ceiling: Double,
    var channel: String,
    var ownerId: String,
    var minimum: Double,
    var expiresAt: Int,
    var discount: Double,
    var ratio: Double,
    var capacity: Int,
    var status: InspectionReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionReviewStatus): Boolean {
        when (status) {
            InspectionReviewStatus.CREATED -> {
                when (next) {
                    InspectionReviewStatus.RETIRED -> return true
                }
                return false
            }
            InspectionReviewStatus.VALIDATED -> return next == InspectionReviewStatus.RETIRED
            InspectionReviewStatus.PUBLISHED -> return next == InspectionReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: InspectionReviewStatus) {
        if (!canMoveTo(next)) {
            error("inspection review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"inspection review"} $code"

    fun adjustedVolume(): Double = round((ceiling + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionReview {
        return InspectionReview(
            code = code,
            name = name,
            ceiling = ceiling,
            channel = channel,
            ownerId = ownerId,
            minimum = minimum,
            expiresAt = expiresAt,
            discount = discount,
            ratio = ratio,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a inspection review in status created. */
fun newInspectionReview(code: String, name: String): InspectionReview {
    return InspectionReview(
        code = code,
        name = name,
        ceiling = 0.0,
        channel = "",
        ownerId = "",
        minimum = 0.0,
        expiresAt = 0,
        discount = 0.0,
        ratio = 0.0,
        capacity = 0,
        status = InspectionReviewStatus.CREATED,
    )
}
