package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability segment records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability segment is in its life cycle. */
enum class AvailabilitySegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A availability segment as the availability team keeps it. */
data class AvailabilitySegment(
    var code: String,
    var name: String,
    var attempts: Int,
    var source: String,
    var quantity: Int,
    var ratio: Double,
    var ownerId: String,
    var pinned: Boolean,
    var revision: Int,
    var updatedAt: Int,
    var status: AvailabilitySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilitySegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilitySegmentStatus): Boolean {
        when (status) {
            AvailabilitySegmentStatus.DRAFT -> {
                when (next) {
                    AvailabilitySegmentStatus.SUSPENDED, AvailabilitySegmentStatus.CLOSED -> return true
                }
                return false
            }
            AvailabilitySegmentStatus.PENDING -> {
                when (next) {
                    AvailabilitySegmentStatus.CLOSED -> return true
                }
                return false
            }
            AvailabilitySegmentStatus.ACTIVE -> {
                when (next) {
                    AvailabilitySegmentStatus.SUSPENDED, AvailabilitySegmentStatus.CLOSED -> return true
                }
                return false
            }
            AvailabilitySegmentStatus.SUSPENDED -> return next == AvailabilitySegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AvailabilitySegmentStatus) {
        if (!canMoveTo(next)) {
            error("availability segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(ratio * attempts.toDouble() * 100) / 100

    fun snapshot(): AvailabilitySegment {
        return AvailabilitySegment(
            code = code,
            name = name,
            attempts = attempts,
            source = source,
            quantity = quantity,
            ratio = ratio,
            ownerId = ownerId,
            pinned = pinned,
            revision = revision,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a availability segment in status draft. */
fun newAvailabilitySegment(code: String, name: String): AvailabilitySegment {
    return AvailabilitySegment(
        code = code,
        name = name,
        attempts = 0,
        source = "",
        quantity = 0,
        ratio = 0.0,
        ownerId = "",
        pinned = false,
        revision = 0,
        updatedAt = 0,
        status = AvailabilitySegmentStatus.DRAFT,
    )
}
