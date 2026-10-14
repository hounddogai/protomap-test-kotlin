package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset allocation is in its life cycle. */
enum class FallbackRouteOffsetAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback route offset allocation and its current status. */
data class FallbackRouteOffsetAllocation(
    var code: String,
    var title: String,
    var lane: String,
    var floorValue: Double,
    var shiftCode: String,
    var visible: Boolean,
    var channel: String,
    var odometer: Int,
    var batchSize: Int,
    var status: FallbackRouteOffsetAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetAllocationStatus): Boolean {
        when (status) {
            FallbackRouteOffsetAllocationStatus.NEW -> return next == FallbackRouteOffsetAllocationStatus.DONE || next == FallbackRouteOffsetAllocationStatus.FAILED
            FallbackRouteOffsetAllocationStatus.QUEUED -> {
                when (next) {
                    FallbackRouteOffsetAllocationStatus.FAILED -> return true
                }
                return false
            }
            FallbackRouteOffsetAllocationStatus.RUNNING -> {
                when (next) {
                    FallbackRouteOffsetAllocationStatus.DONE, FallbackRouteOffsetAllocationStatus.FAILED -> return true
                }
                return false
            }
            FallbackRouteOffsetAllocationStatus.DONE -> {
                when (next) {
                    FallbackRouteOffsetAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback route offset allocation"} $code"

    fun projectedTotal(): Double = round((floorValue + batchSize.toDouble()) * 100) / 100

    fun snapshot(): FallbackRouteOffsetAllocation {
        return FallbackRouteOffsetAllocation(
            code = code,
            title = title,
            lane = lane,
            floorValue = floorValue,
            shiftCode = shiftCode,
            visible = visible,
            channel = channel,
            odometer = odometer,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback route offset allocation in status new. */
fun newFallbackRouteOffsetAllocation(code: String, title: String): FallbackRouteOffsetAllocation {
    return FallbackRouteOffsetAllocation(
        code = code,
        title = title,
        lane = "",
        floorValue = 0.0,
        shiftCode = "",
        visible = false,
        channel = "",
        odometer = 0,
        batchSize = 0,
        status = FallbackRouteOffsetAllocationStatus.NEW,
    )
}
