package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback capacity request is in its life cycle. */
enum class FallbackCapacityRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback capacity request as the capacity team keeps it. */
data class FallbackCapacityRequest(
    var code: String,
    var title: String,
    var ceiling: Double,
    var ratio: Double,
    var routeCode: String,
    var stopCount: Int,
    var limit: Int,
    var rate: Double,
    var region: String,
    var fuelLiters: Double,
    var category: String,
    var status: FallbackCapacityRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCapacityRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCapacityRequestStatus): Boolean {
        when (status) {
            FallbackCapacityRequestStatus.REQUESTED -> return next == FallbackCapacityRequestStatus.EXPIRED
            FallbackCapacityRequestStatus.GRANTED -> return next == FallbackCapacityRequestStatus.EXPIRED
            FallbackCapacityRequestStatus.REVOKED -> return next == FallbackCapacityRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackCapacityRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback capacity request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((limit.toDouble() + rate) * 100) / 100

    fun snapshot(): FallbackCapacityRequest {
        return FallbackCapacityRequest(
            code = code,
            title = title,
            ceiling = ceiling,
            ratio = ratio,
            routeCode = routeCode,
            stopCount = stopCount,
            limit = limit,
            rate = rate,
            region = region,
            fuelLiters = fuelLiters,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback capacity request in status requested. */
fun newFallbackCapacityRequest(code: String, title: String): FallbackCapacityRequest {
    return FallbackCapacityRequest(
        code = code,
        title = title,
        ceiling = 0.0,
        ratio = 0.0,
        routeCode = "",
        stopCount = 0,
        limit = 0,
        rate = 0.0,
        region = "",
        fuelLiters = 0.0,
        category = "",
        status = FallbackCapacityRequestStatus.REQUESTED,
    )
}
