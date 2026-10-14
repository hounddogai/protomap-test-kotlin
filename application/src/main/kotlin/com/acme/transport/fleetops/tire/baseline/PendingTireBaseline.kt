package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tire baseline is in its life cycle. */
enum class PendingTireBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending tire baseline, with what the planners need to act on it. */
data class PendingTireBaseline(
    var code: String,
    var label: String,
    var notes: String,
    var expiresAt: Int,
    var createdAt: Int,
    var segment: String,
    var loadKg: Double,
    var status: PendingTireBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTireBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTireBaselineStatus): Boolean {
        when (status) {
            PendingTireBaselineStatus.DRAFT -> return next == PendingTireBaselineStatus.SUSPENDED
            PendingTireBaselineStatus.PENDING -> {
                when (next) {
                    PendingTireBaselineStatus.ACTIVE, PendingTireBaselineStatus.CLOSED -> return true
                }
                return false
            }
            PendingTireBaselineStatus.ACTIVE -> return next == PendingTireBaselineStatus.SUSPENDED
            PendingTireBaselineStatus.SUSPENDED -> {
                when (next) {
                    PendingTireBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTireBaselineStatus) {
        if (!canMoveTo(next)) {
            error("pending tire baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round(expiresAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): PendingTireBaseline {
        return PendingTireBaseline(
            code = code,
            label = label,
            notes = notes,
            expiresAt = expiresAt,
            createdAt = createdAt,
            segment = segment,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending tire baseline in status draft. */
fun newPendingTireBaseline(code: String, label: String): PendingTireBaseline {
    return PendingTireBaseline(
        code = code,
        label = label,
        notes = "",
        expiresAt = 0,
        createdAt = 0,
        segment = "",
        loadKg = 0.0,
        status = PendingTireBaselineStatus.DRAFT,
    )
}
