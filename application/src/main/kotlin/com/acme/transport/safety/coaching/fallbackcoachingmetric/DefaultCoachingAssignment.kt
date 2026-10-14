package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default coaching assignment is in its life cycle. */
enum class DefaultCoachingAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default coaching assignment. Its code is unique within a tenant. */
data class DefaultCoachingAssignment(
    var code: String,
    var label: String,
    var attempts: Int,
    var externalRef: String,
    var approved: Boolean,
    var createdAt: Int,
    var score: Double,
    var status: DefaultCoachingAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingAssignmentStatus): Boolean {
        when (status) {
            DefaultCoachingAssignmentStatus.IDLE -> return next == DefaultCoachingAssignmentStatus.READY || next == DefaultCoachingAssignmentStatus.STOPPED
            DefaultCoachingAssignmentStatus.WARMING -> {
                when (next) {
                    DefaultCoachingAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            DefaultCoachingAssignmentStatus.READY -> return next == DefaultCoachingAssignmentStatus.DRAINING
            DefaultCoachingAssignmentStatus.DRAINING -> {
                when (next) {
                    DefaultCoachingAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCoachingAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default coaching assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round(createdAt.toDouble() * score * 100) / 100

    fun snapshot(): DefaultCoachingAssignment {
        return DefaultCoachingAssignment(
            code = code,
            label = label,
            attempts = attempts,
            externalRef = externalRef,
            approved = approved,
            createdAt = createdAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default coaching assignment in status idle. */
fun newDefaultCoachingAssignment(code: String, label: String): DefaultCoachingAssignment {
    return DefaultCoachingAssignment(
        code = code,
        label = label,
        attempts = 0,
        externalRef = "",
        approved = false,
        createdAt = 0,
        score = 0.0,
        status = DefaultCoachingAssignmentStatus.IDLE,
    )
}
