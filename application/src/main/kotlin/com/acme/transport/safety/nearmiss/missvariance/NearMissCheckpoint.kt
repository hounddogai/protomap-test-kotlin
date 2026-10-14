package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss checkpoint is in its life cycle. */
enum class NearMissCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A near miss checkpoint. Its code is unique within a tenant. */
data class NearMissCheckpoint(
    var code: String,
    var label: String,
    var ownerId: String,
    var durationMinutes: Int,
    var notes: String,
    var startsAt: Int,
    var margin: Double,
    var cost: Double,
    var endsAt: Int,
    var pinned: Boolean,
    var amount: Double,
    var status: NearMissCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissCheckpointStatus): Boolean {
        when (status) {
            NearMissCheckpointStatus.CREATED -> return next == NearMissCheckpointStatus.VALIDATED
            NearMissCheckpointStatus.VALIDATED -> return next == NearMissCheckpointStatus.PUBLISHED || next == NearMissCheckpointStatus.RETIRED
            NearMissCheckpointStatus.PUBLISHED -> return next == NearMissCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NearMissCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("near miss checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(endsAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): NearMissCheckpoint {
        return NearMissCheckpoint(
            code = code,
            label = label,
            ownerId = ownerId,
            durationMinutes = durationMinutes,
            notes = notes,
            startsAt = startsAt,
            margin = margin,
            cost = cost,
            endsAt = endsAt,
            pinned = pinned,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a near miss checkpoint in status created. */
fun newNearMissCheckpoint(code: String, label: String): NearMissCheckpoint {
    return NearMissCheckpoint(
        code = code,
        label = label,
        ownerId = "",
        durationMinutes = 0,
        notes = "",
        startsAt = 0,
        margin = 0.0,
        cost = 0.0,
        endsAt = 0,
        pinned = false,
        amount = 0.0,
        status = NearMissCheckpointStatus.CREATED,
    )
}
