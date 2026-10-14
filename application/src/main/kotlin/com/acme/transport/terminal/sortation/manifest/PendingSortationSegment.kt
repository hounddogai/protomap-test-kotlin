package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending sortation segment is in its life cycle. */
enum class PendingSortationSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending sortation segment and its current status. */
data class PendingSortationSegment(
    var code: String,
    var name: String,
    var description: String,
    var priority: Int,
    var windowDays: Int,
    var floorValue: Double,
    var externalRef: String,
    var expiresAt: Int,
    var lane: String,
    var score: Double,
    var status: PendingSortationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSortationSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSortationSegmentStatus): Boolean {
        when (status) {
            PendingSortationSegmentStatus.REQUESTED -> return next == PendingSortationSegmentStatus.REVOKED
            PendingSortationSegmentStatus.GRANTED -> {
                when (next) {
                    PendingSortationSegmentStatus.REVOKED -> return true
                }
                return false
            }
            PendingSortationSegmentStatus.REVOKED -> return next == PendingSortationSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingSortationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("pending sortation segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((floorValue + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PendingSortationSegment {
        return PendingSortationSegment(
            code = code,
            name = name,
            description = description,
            priority = priority,
            windowDays = windowDays,
            floorValue = floorValue,
            externalRef = externalRef,
            expiresAt = expiresAt,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending sortation segment in status requested. */
fun newPendingSortationSegment(code: String, name: String): PendingSortationSegment {
    return PendingSortationSegment(
        code = code,
        name = name,
        description = "",
        priority = 0,
        windowDays = 0,
        floorValue = 0.0,
        externalRef = "",
        expiresAt = 0,
        lane = "",
        score = 0.0,
        status = PendingSortationSegmentStatus.REQUESTED,
    )
}
