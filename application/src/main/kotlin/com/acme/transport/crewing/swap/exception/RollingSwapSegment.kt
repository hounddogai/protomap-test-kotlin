package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling swap segment is in its life cycle. */
enum class RollingSwapSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling swap segment as the swap team keeps it. */
data class RollingSwapSegment(
    var code: String,
    var name: String,
    var discount: Double,
    var description: String,
    var maximum: Double,
    var priority: Int,
    var capacity: Int,
    var pinned: Boolean,
    var driverId: String,
    var attempts: Int,
    var segment: String,
    var status: RollingSwapSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapSegmentStatus): Boolean {
        when (status) {
            RollingSwapSegmentStatus.REQUESTED -> {
                when (next) {
                    RollingSwapSegmentStatus.REVOKED -> return true
                }
                return false
            }
            RollingSwapSegmentStatus.GRANTED -> return next == RollingSwapSegmentStatus.EXPIRED
            RollingSwapSegmentStatus.REVOKED -> {
                when (next) {
                    RollingSwapSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingSwapSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((attempts.toDouble() + discount) * 100) / 100

    fun snapshot(): RollingSwapSegment {
        return RollingSwapSegment(
            code = code,
            name = name,
            discount = discount,
            description = description,
            maximum = maximum,
            priority = priority,
            capacity = capacity,
            pinned = pinned,
            driverId = driverId,
            attempts = attempts,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling swap segment in status requested. */
fun newRollingSwapSegment(code: String, name: String): RollingSwapSegment {
    return RollingSwapSegment(
        code = code,
        name = name,
        discount = 0.0,
        description = "",
        maximum = 0.0,
        priority = 0,
        capacity = 0,
        pinned = false,
        driverId = "",
        attempts = 0,
        segment = "",
        status = RollingSwapSegmentStatus.REQUESTED,
    )
}
