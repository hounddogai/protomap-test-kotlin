package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability review is in its life cycle. */
enum class AvailabilityReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A availability review as the availability team keeps it. */
data class AvailabilityReview(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var amount: Double,
    var archived: Boolean,
    var vehicleId: String,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var status: AvailabilityReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityReviewStatus): Boolean {
        when (status) {
            AvailabilityReviewStatus.PROPOSED -> {
                when (next) {
                    AvailabilityReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            AvailabilityReviewStatus.APPROVED -> {
                when (next) {
                    AvailabilityReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            AvailabilityReviewStatus.REJECTED -> return next == AvailabilityReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AvailabilityReviewStatus) {
        if (!canMoveTo(next)) {
            error("availability review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"availability review"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((sequence.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityReview {
        return AvailabilityReview(
            code = code,
            name = name,
            updatedAt = updatedAt,
            amount = amount,
            archived = archived,
            vehicleId = vehicleId,
            sequence = sequence,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability review in status proposed. */
fun newAvailabilityReview(code: String, name: String): AvailabilityReview {
    return AvailabilityReview(
        code = code,
        name = name,
        updatedAt = 0,
        amount = 0.0,
        archived = false,
        vehicleId = "",
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        status = AvailabilityReviewStatus.PROPOSED,
    )
}
