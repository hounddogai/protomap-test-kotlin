package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel segment records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel segment is in its life cycle. */
enum class ParcelSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A parcel segment as the parcel team keeps it. */
data class ParcelSegment(
    var code: String,
    var label: String,
    var reviewer: String,
    var score: Double,
    var priority: Int,
    var attempts: Int,
    var status: ParcelSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelSegmentStatus): Boolean {
        when (status) {
            ParcelSegmentStatus.REQUESTED -> return next == ParcelSegmentStatus.GRANTED
            ParcelSegmentStatus.GRANTED -> {
                when (next) {
                    ParcelSegmentStatus.REVOKED, ParcelSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelSegmentStatus.REVOKED -> return next == ParcelSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ParcelSegmentStatus) {
        if (!canMoveTo(next)) {
            error("parcel segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"parcel segment"} $code"

    fun adjustedValue(): Double = round((attempts.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): ParcelSegment {
        return ParcelSegment(
            code = code,
            label = label,
            reviewer = reviewer,
            score = score,
            priority = priority,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel segment in status requested. */
fun newParcelSegment(code: String, label: String): ParcelSegment {
    return ParcelSegment(
        code = code,
        label = label,
        reviewer = "",
        score = 0.0,
        priority = 0,
        attempts = 0,
        status = ParcelSegmentStatus.REQUESTED,
    )
}
