package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset variance is in its life cycle. */
enum class FallbackRouteOffsetVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback route offset variance as the route offset team keeps it. */
data class FallbackRouteOffsetVariance(
    var code: String,
    var title: String,
    var checksum: String,
    var pallets: Int,
    var rate: Double,
    var lane: String,
    var sequence: Int,
    var floorValue: Double,
    var etaMinutes: Int,
    var capacity: Int,
    var status: FallbackRouteOffsetVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetVarianceStatus): Boolean {
        when (status) {
            FallbackRouteOffsetVarianceStatus.PLANNED -> {
                when (next) {
                    FallbackRouteOffsetVarianceStatus.SCHEDULED, FallbackRouteOffsetVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            FallbackRouteOffsetVarianceStatus.SCHEDULED -> return next == FallbackRouteOffsetVarianceStatus.IN_PROGRESS
            FallbackRouteOffsetVarianceStatus.IN_PROGRESS -> return next == FallbackRouteOffsetVarianceStatus.COMPLETED || next == FallbackRouteOffsetVarianceStatus.CANCELLED
            FallbackRouteOffsetVarianceStatus.COMPLETED -> return next == FallbackRouteOffsetVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((pallets.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): FallbackRouteOffsetVariance {
        return FallbackRouteOffsetVariance(
            code = code,
            title = title,
            checksum = checksum,
            pallets = pallets,
            rate = rate,
            lane = lane,
            sequence = sequence,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback route offset variance in status planned. */
fun newFallbackRouteOffsetVariance(code: String, title: String): FallbackRouteOffsetVariance {
    return FallbackRouteOffsetVariance(
        code = code,
        title = title,
        checksum = "",
        pallets = 0,
        rate = 0.0,
        lane = "",
        sequence = 0,
        floorValue = 0.0,
        etaMinutes = 0,
        capacity = 0,
        status = FallbackRouteOffsetVarianceStatus.PLANNED,
    )
}
