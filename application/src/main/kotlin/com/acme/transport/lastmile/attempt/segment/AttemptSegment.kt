package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt segment records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt segment is in its life cycle. */
enum class AttemptSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A attempt segment. Its code is unique within a tenant. */
data class AttemptSegment(
    var code: String,
    var name: String,
    var notes: String,
    var active: Boolean,
    var currency: String,
    var threshold: Double,
    var title: String,
    var ownerId: String,
    var createdAt: Int,
    var status: AttemptSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptSegmentStatus): Boolean {
        when (status) {
            AttemptSegmentStatus.PROPOSED -> {
                when (next) {
                    AttemptSegmentStatus.APPROVED, AttemptSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            AttemptSegmentStatus.APPROVED -> return next == AttemptSegmentStatus.WITHDRAWN
            AttemptSegmentStatus.REJECTED -> return next == AttemptSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AttemptSegmentStatus) {
        if (!canMoveTo(next)) {
            error("attempt segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"attempt segment"} $code"

    fun adjustedLoad(): Double = round((threshold + createdAt.toDouble()) * 100) / 100

    fun snapshot(): AttemptSegment {
        return AttemptSegment(
            code = code,
            name = name,
            notes = notes,
            active = active,
            currency = currency,
            threshold = threshold,
            title = title,
            ownerId = ownerId,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a attempt segment in status proposed. */
fun newAttemptSegment(code: String, name: String): AttemptSegment {
    return AttemptSegment(
        code = code,
        name = name,
        notes = "",
        active = false,
        currency = "",
        threshold = 0.0,
        title = "",
        ownerId = "",
        createdAt = 0,
        status = AttemptSegmentStatus.PROPOSED,
    )
}
