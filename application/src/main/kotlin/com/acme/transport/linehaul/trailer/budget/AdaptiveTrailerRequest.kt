package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive trailer request is in its life cycle. */
enum class AdaptiveTrailerRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive trailer request as the trailer team keeps it. */
data class AdaptiveTrailerRequest(
    var code: String,
    var name: String,
    var lane: String,
    var timezone: String,
    var cost: Double,
    var expiresAt: Int,
    var floorValue: Double,
    var pinned: Boolean,
    var unit: String,
    var phone: String,
    var status: AdaptiveTrailerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerRequestStatus): Boolean {
        when (status) {
            AdaptiveTrailerRequestStatus.PROPOSED -> return next == AdaptiveTrailerRequestStatus.REJECTED || next == AdaptiveTrailerRequestStatus.WITHDRAWN
            AdaptiveTrailerRequestStatus.APPROVED -> {
                when (next) {
                    AdaptiveTrailerRequestStatus.REJECTED, AdaptiveTrailerRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveTrailerRequestStatus.REJECTED -> {
                when (next) {
                    AdaptiveTrailerRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerRequestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer request $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive trailer request"} $code"

    fun netValue(): Double = round(cost * floorValue * 100) / 100

    fun snapshot(): AdaptiveTrailerRequest {
        return AdaptiveTrailerRequest(
            code = code,
            name = name,
            lane = lane,
            timezone = timezone,
            cost = cost,
            expiresAt = expiresAt,
            floorValue = floorValue,
            pinned = pinned,
            unit = unit,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive trailer request in status proposed. */
fun newAdaptiveTrailerRequest(code: String, name: String): AdaptiveTrailerRequest {
    return AdaptiveTrailerRequest(
        code = code,
        name = name,
        lane = "",
        timezone = "",
        cost = 0.0,
        expiresAt = 0,
        floorValue = 0.0,
        pinned = false,
        unit = "",
        phone = "",
        status = AdaptiveTrailerRequestStatus.PROPOSED,
    )
}
