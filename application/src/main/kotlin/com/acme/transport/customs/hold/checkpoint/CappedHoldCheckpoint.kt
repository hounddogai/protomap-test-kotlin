package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped hold checkpoint records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a capped hold checkpoint is in its life cycle. */
enum class CappedHoldCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped hold checkpoint. Its code is unique within a tenant. */
data class CappedHoldCheckpoint(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var margin: Double,
    var cost: Double,
    var approved: Boolean,
    var phone: String,
    var minimum: Double,
    var status: CappedHoldCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHoldCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHoldCheckpointStatus): Boolean {
        when (status) {
            CappedHoldCheckpointStatus.REQUESTED -> return next == CappedHoldCheckpointStatus.GRANTED || next == CappedHoldCheckpointStatus.EXPIRED
            CappedHoldCheckpointStatus.GRANTED -> return next == CappedHoldCheckpointStatus.EXPIRED
            CappedHoldCheckpointStatus.REVOKED -> return next == CappedHoldCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedHoldCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped hold checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((minimum - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedHoldCheckpoint {
        return CappedHoldCheckpoint(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            margin = margin,
            cost = cost,
            approved = approved,
            phone = phone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped hold checkpoint in status requested. */
fun newCappedHoldCheckpoint(code: String, title: String): CappedHoldCheckpoint {
    return CappedHoldCheckpoint(
        code = code,
        title = title,
        durationMinutes = 0,
        margin = 0.0,
        cost = 0.0,
        approved = false,
        phone = "",
        minimum = 0.0,
        status = CappedHoldCheckpointStatus.REQUESTED,
    )
}
