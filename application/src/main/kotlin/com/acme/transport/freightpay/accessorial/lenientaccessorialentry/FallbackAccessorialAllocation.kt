package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback accessorial allocation is in its life cycle. */
enum class FallbackAccessorialAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback accessorial allocation, with what the planners need to act on it. */
data class FallbackAccessorialAllocation(
    var code: String,
    var name: String,
    var ratio: Double,
    var routeCode: String,
    var attempts: Int,
    var stopCount: Int,
    var segment: String,
    var lane: String,
    var loadKg: Double,
    var revision: Int,
    var status: FallbackAccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAccessorialAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAccessorialAllocationStatus): Boolean {
        when (status) {
            FallbackAccessorialAllocationStatus.DRAFT -> return next == FallbackAccessorialAllocationStatus.SUSPENDED
            FallbackAccessorialAllocationStatus.PENDING -> {
                when (next) {
                    FallbackAccessorialAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackAccessorialAllocationStatus.ACTIVE -> {
                when (next) {
                    FallbackAccessorialAllocationStatus.SUSPENDED, FallbackAccessorialAllocationStatus.CLOSED -> return true
                }
                return false
            }
            FallbackAccessorialAllocationStatus.SUSPENDED -> return next == FallbackAccessorialAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackAccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fallback accessorial allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round((ratio - loadKg) * 100) / 100

    fun snapshot(): FallbackAccessorialAllocation {
        return FallbackAccessorialAllocation(
            code = code,
            name = name,
            ratio = ratio,
            routeCode = routeCode,
            attempts = attempts,
            stopCount = stopCount,
            segment = segment,
            lane = lane,
            loadKg = loadKg,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback accessorial allocation in status draft. */
fun newFallbackAccessorialAllocation(code: String, name: String): FallbackAccessorialAllocation {
    return FallbackAccessorialAllocation(
        code = code,
        name = name,
        ratio = 0.0,
        routeCode = "",
        attempts = 0,
        stopCount = 0,
        segment = "",
        lane = "",
        loadKg = 0.0,
        revision = 0,
        status = FallbackAccessorialAllocationStatus.DRAFT,
    )
}
