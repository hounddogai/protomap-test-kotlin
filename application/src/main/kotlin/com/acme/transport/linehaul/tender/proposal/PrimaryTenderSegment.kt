package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tender segment is in its life cycle. */
enum class PrimaryTenderSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary tender segment, with what the planners need to act on it. */
data class PrimaryTenderSegment(
    var code: String,
    var title: String,
    var discount: Double,
    var lane: String,
    var floorValue: Double,
    var depot: String,
    var approved: Boolean,
    var updatedAt: Int,
    var durationMinutes: Int,
    var source: String,
    var status: PrimaryTenderSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderSegmentStatus): Boolean {
        when (status) {
            PrimaryTenderSegmentStatus.PROPOSED -> {
                when (next) {
                    PrimaryTenderSegmentStatus.APPROVED, PrimaryTenderSegmentStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryTenderSegmentStatus.APPROVED -> return next == PrimaryTenderSegmentStatus.REJECTED || next == PrimaryTenderSegmentStatus.WITHDRAWN
            PrimaryTenderSegmentStatus.REJECTED -> {
                when (next) {
                    PrimaryTenderSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTenderSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary tender segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary tender segment"} $code"

    fun adjustedValue(): Double = round((floorValue + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTenderSegment {
        return PrimaryTenderSegment(
            code = code,
            title = title,
            discount = discount,
            lane = lane,
            floorValue = floorValue,
            depot = depot,
            approved = approved,
            updatedAt = updatedAt,
            durationMinutes = durationMinutes,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary tender segment in status proposed. */
fun newPrimaryTenderSegment(code: String, title: String): PrimaryTenderSegment {
    return PrimaryTenderSegment(
        code = code,
        title = title,
        discount = 0.0,
        lane = "",
        floorValue = 0.0,
        depot = "",
        approved = false,
        updatedAt = 0,
        durationMinutes = 0,
        source = "",
        status = PrimaryTenderSegmentStatus.PROPOSED,
    )
}
