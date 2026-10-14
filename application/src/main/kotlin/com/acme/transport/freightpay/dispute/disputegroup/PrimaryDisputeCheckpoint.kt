package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dispute checkpoint is in its life cycle. */
enum class PrimaryDisputeCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary dispute checkpoint. Its code is unique within a tenant. */
data class PrimaryDisputeCheckpoint(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var discount: Double,
    var dock: String,
    var attempts: Int,
    var trailerId: String,
    var notes: String,
    var expiresAt: Int,
    var country: String,
    var status: PrimaryDisputeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDisputeCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDisputeCheckpointStatus): Boolean {
        when (status) {
            PrimaryDisputeCheckpointStatus.PROPOSED -> {
                when (next) {
                    PrimaryDisputeCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryDisputeCheckpointStatus.APPROVED -> return next == PrimaryDisputeCheckpointStatus.REJECTED || next == PrimaryDisputeCheckpointStatus.WITHDRAWN
            PrimaryDisputeCheckpointStatus.REJECTED -> {
                when (next) {
                    PrimaryDisputeCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDisputeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("primary dispute checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary dispute checkpoint"} $code"

    fun adjustedVolume(): Double = round(expiresAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): PrimaryDisputeCheckpoint {
        return PrimaryDisputeCheckpoint(
            code = code,
            label = label,
            distanceKm = distanceKm,
            discount = discount,
            dock = dock,
            attempts = attempts,
            trailerId = trailerId,
            notes = notes,
            expiresAt = expiresAt,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary dispute checkpoint in status proposed. */
fun newPrimaryDisputeCheckpoint(code: String, label: String): PrimaryDisputeCheckpoint {
    return PrimaryDisputeCheckpoint(
        code = code,
        label = label,
        distanceKm = 0.0,
        discount = 0.0,
        dock = "",
        attempts = 0,
        trailerId = "",
        notes = "",
        expiresAt = 0,
        country = "",
        status = PrimaryDisputeCheckpointStatus.PROPOSED,
    )
}
