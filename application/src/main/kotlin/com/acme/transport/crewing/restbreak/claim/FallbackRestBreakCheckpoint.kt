package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rest break checkpoint is in its life cycle. */
enum class FallbackRestBreakCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback rest break checkpoint. Its code is unique within a tenant. */
data class FallbackRestBreakCheckpoint(
    var code: String,
    var title: String,
    var pallets: Int,
    var sequence: Int,
    var locale: String,
    var version: Int,
    var trailerId: String,
    var archived: Boolean,
    var loadKg: Double,
    var ownerId: String,
    var weight: Double,
    var status: FallbackRestBreakCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRestBreakCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRestBreakCheckpointStatus): Boolean {
        when (status) {
            FallbackRestBreakCheckpointStatus.OPEN -> {
                when (next) {
                    FallbackRestBreakCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRestBreakCheckpointStatus.IN_REVIEW -> return next == FallbackRestBreakCheckpointStatus.ARCHIVED
            FallbackRestBreakCheckpointStatus.RESOLVED -> {
                when (next) {
                    FallbackRestBreakCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRestBreakCheckpointStatus.REOPENED -> return next == FallbackRestBreakCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackRestBreakCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fallback rest break checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round((loadKg - sequence.toDouble()) * 100) / 100

    fun snapshot(): FallbackRestBreakCheckpoint {
        return FallbackRestBreakCheckpoint(
            code = code,
            title = title,
            pallets = pallets,
            sequence = sequence,
            locale = locale,
            version = version,
            trailerId = trailerId,
            archived = archived,
            loadKg = loadKg,
            ownerId = ownerId,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback rest break checkpoint in status open. */
fun newFallbackRestBreakCheckpoint(code: String, title: String): FallbackRestBreakCheckpoint {
    return FallbackRestBreakCheckpoint(
        code = code,
        title = title,
        pallets = 0,
        sequence = 0,
        locale = "",
        version = 0,
        trailerId = "",
        archived = false,
        loadKg = 0.0,
        ownerId = "",
        weight = 0.0,
        status = FallbackRestBreakCheckpointStatus.OPEN,
    )
}
