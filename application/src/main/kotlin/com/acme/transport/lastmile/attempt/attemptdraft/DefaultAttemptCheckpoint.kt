package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default attempt checkpoint is in its life cycle. */
enum class DefaultAttemptCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default attempt checkpoint and its current status. */
data class DefaultAttemptCheckpoint(
    var code: String,
    var label: String,
    var category: String,
    var source: String,
    var budget: Double,
    var archived: Boolean,
    var fuelLiters: Double,
    var country: String,
    var status: DefaultAttemptCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAttemptCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAttemptCheckpointStatus): Boolean {
        when (status) {
            DefaultAttemptCheckpointStatus.REQUESTED -> return next == DefaultAttemptCheckpointStatus.EXPIRED
            DefaultAttemptCheckpointStatus.GRANTED -> return next == DefaultAttemptCheckpointStatus.REVOKED || next == DefaultAttemptCheckpointStatus.EXPIRED
            DefaultAttemptCheckpointStatus.REVOKED -> {
                when (next) {
                    DefaultAttemptCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultAttemptCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("default attempt checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round((budget + fuelLiters) * 100) / 100

    fun snapshot(): DefaultAttemptCheckpoint {
        return DefaultAttemptCheckpoint(
            code = code,
            label = label,
            category = category,
            source = source,
            budget = budget,
            archived = archived,
            fuelLiters = fuelLiters,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default attempt checkpoint in status requested. */
fun newDefaultAttemptCheckpoint(code: String, label: String): DefaultAttemptCheckpoint {
    return DefaultAttemptCheckpoint(
        code = code,
        label = label,
        category = "",
        source = "",
        budget = 0.0,
        archived = false,
        fuelLiters = 0.0,
        country = "",
        status = DefaultAttemptCheckpointStatus.REQUESTED,
    )
}
