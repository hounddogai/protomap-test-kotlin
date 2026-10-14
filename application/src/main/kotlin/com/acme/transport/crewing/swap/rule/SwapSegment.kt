package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap segment is in its life cycle. */
enum class SwapSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a swap segment and its current status. */
data class SwapSegment(
    var code: String,
    var title: String,
    var shiftCode: String,
    var vehicleId: String,
    var distanceKm: Double,
    var sequence: Int,
    var minimum: Double,
    var maximum: Double,
    var status: SwapSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapSegmentStatus): Boolean {
        when (status) {
            SwapSegmentStatus.DRAFT -> {
                when (next) {
                    SwapSegmentStatus.ACTIVE -> return true
                }
                return false
            }
            SwapSegmentStatus.PENDING -> return next == SwapSegmentStatus.ACTIVE || next == SwapSegmentStatus.SUSPENDED
            SwapSegmentStatus.ACTIVE -> return next == SwapSegmentStatus.SUSPENDED || next == SwapSegmentStatus.CLOSED
            SwapSegmentStatus.SUSPENDED -> {
                when (next) {
                    SwapSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapSegmentStatus) {
        if (!canMoveTo(next)) {
            error("swap segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round(distanceKm * minimum * 100) / 100

    fun snapshot(): SwapSegment {
        return SwapSegment(
            code = code,
            title = title,
            shiftCode = shiftCode,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            sequence = sequence,
            minimum = minimum,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap segment in status draft. */
fun newSwapSegment(code: String, title: String): SwapSegment {
    return SwapSegment(
        code = code,
        title = title,
        shiftCode = "",
        vehicleId = "",
        distanceKm = 0.0,
        sequence = 0,
        minimum = 0.0,
        maximum = 0.0,
        status = SwapSegmentStatus.DRAFT,
    )
}
