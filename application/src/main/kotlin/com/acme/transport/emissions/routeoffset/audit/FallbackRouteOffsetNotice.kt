package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset notice is in its life cycle. */
enum class FallbackRouteOffsetNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback route offset notice as the route offset team keeps it. */
data class FallbackRouteOffsetNotice(
    var code: String,
    var name: String,
    var approved: Boolean,
    var distanceKm: Double,
    var lane: String,
    var discount: Double,
    var segment: String,
    var status: FallbackRouteOffsetNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetNoticeStatus): Boolean {
        when (status) {
            FallbackRouteOffsetNoticeStatus.IDLE -> return next == FallbackRouteOffsetNoticeStatus.STOPPED
            FallbackRouteOffsetNoticeStatus.WARMING -> {
                when (next) {
                    FallbackRouteOffsetNoticeStatus.DRAINING -> return true
                }
                return false
            }
            FallbackRouteOffsetNoticeStatus.READY -> return next == FallbackRouteOffsetNoticeStatus.STOPPED
            FallbackRouteOffsetNoticeStatus.DRAINING -> return next == FallbackRouteOffsetNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((distanceKm + discount) * 100) / 100

    fun snapshot(): FallbackRouteOffsetNotice {
        return FallbackRouteOffsetNotice(
            code = code,
            name = name,
            approved = approved,
            distanceKm = distanceKm,
            lane = lane,
            discount = discount,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback route offset notice in status idle. */
fun newFallbackRouteOffsetNotice(code: String, name: String): FallbackRouteOffsetNotice {
    return FallbackRouteOffsetNotice(
        code = code,
        name = name,
        approved = false,
        distanceKm = 0.0,
        lane = "",
        discount = 0.0,
        segment = "",
        status = FallbackRouteOffsetNoticeStatus.IDLE,
    )
}
