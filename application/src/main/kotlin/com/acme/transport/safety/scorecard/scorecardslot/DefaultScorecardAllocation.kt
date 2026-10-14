package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default scorecard allocation is in its life cycle. */
enum class DefaultScorecardAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default scorecard allocation and its current status. */
data class DefaultScorecardAllocation(
    var code: String,
    var title: String,
    var phone: String,
    var capacity: Int,
    var depot: String,
    var currency: String,
    var ownerId: String,
    var floorValue: Double,
    var quantity: Int,
    var visible: Boolean,
    var distanceKm: Double,
    var status: DefaultScorecardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultScorecardAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultScorecardAllocationStatus): Boolean {
        when (status) {
            DefaultScorecardAllocationStatus.CREATED -> {
                when (next) {
                    DefaultScorecardAllocationStatus.VALIDATED, DefaultScorecardAllocationStatus.RETIRED -> return true
                }
                return false
            }
            DefaultScorecardAllocationStatus.VALIDATED -> return next == DefaultScorecardAllocationStatus.PUBLISHED || next == DefaultScorecardAllocationStatus.RETIRED
            DefaultScorecardAllocationStatus.PUBLISHED -> return next == DefaultScorecardAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultScorecardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default scorecard allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default scorecard allocation"} $code"

    fun netVolume(): Double = round((capacity.toDouble() + floorValue) * 100) / 100

    fun snapshot(): DefaultScorecardAllocation {
        return DefaultScorecardAllocation(
            code = code,
            title = title,
            phone = phone,
            capacity = capacity,
            depot = depot,
            currency = currency,
            ownerId = ownerId,
            floorValue = floorValue,
            quantity = quantity,
            visible = visible,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default scorecard allocation in status created. */
fun newDefaultScorecardAllocation(code: String, title: String): DefaultScorecardAllocation {
    return DefaultScorecardAllocation(
        code = code,
        title = title,
        phone = "",
        capacity = 0,
        depot = "",
        currency = "",
        ownerId = "",
        floorValue = 0.0,
        quantity = 0,
        visible = false,
        distanceKm = 0.0,
        status = DefaultScorecardAllocationStatus.CREATED,
    )
}
