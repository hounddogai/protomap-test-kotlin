package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching mapping is in its life cycle. */
enum class CoachingMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a coaching mapping and its current status. */
data class CoachingMapping(
    var code: String,
    var title: String,
    var quantity: Int,
    var depot: String,
    var archived: Boolean,
    var amount: Double,
    var status: CoachingMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingMappingStatus): Boolean {
        when (status) {
            CoachingMappingStatus.CREATED -> {
                when (next) {
                    CoachingMappingStatus.VALIDATED -> return true
                }
                return false
            }
            CoachingMappingStatus.VALIDATED -> return next == CoachingMappingStatus.PUBLISHED || next == CoachingMappingStatus.RETIRED
            CoachingMappingStatus.PUBLISHED -> return next == CoachingMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CoachingMappingStatus) {
        if (!canMoveTo(next)) {
            error("coaching mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round(amount * quantity.toDouble() * 100) / 100

    fun snapshot(): CoachingMapping {
        return CoachingMapping(
            code = code,
            title = title,
            quantity = quantity,
            depot = depot,
            archived = archived,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a coaching mapping in status created. */
fun newCoachingMapping(code: String, title: String): CoachingMapping {
    return CoachingMapping(
        code = code,
        title = title,
        quantity = 0,
        depot = "",
        archived = false,
        amount = 0.0,
        status = CoachingMappingStatus.CREATED,
    )
}
