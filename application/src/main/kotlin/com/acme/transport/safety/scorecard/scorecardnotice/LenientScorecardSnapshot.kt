package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient scorecard snapshot is in its life cycle. */
enum class LenientScorecardSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient scorecard snapshot as the scorecard team keeps it. */
data class LenientScorecardSnapshot(
    var code: String,
    var title: String,
    var attempts: Int,
    var dock: String,
    var fuelLiters: Double,
    var status: LenientScorecardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientScorecardSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientScorecardSnapshotStatus): Boolean {
        when (status) {
            LenientScorecardSnapshotStatus.DRAFT -> {
                when (next) {
                    LenientScorecardSnapshotStatus.PENDING, LenientScorecardSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientScorecardSnapshotStatus.PENDING -> return next == LenientScorecardSnapshotStatus.SUSPENDED
            LenientScorecardSnapshotStatus.ACTIVE -> return next == LenientScorecardSnapshotStatus.SUSPENDED || next == LenientScorecardSnapshotStatus.CLOSED
            LenientScorecardSnapshotStatus.SUSPENDED -> {
                when (next) {
                    LenientScorecardSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientScorecardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lenient scorecard snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((fuelLiters + attempts.toDouble()) * 100) / 100

    fun snapshot(): LenientScorecardSnapshot {
        return LenientScorecardSnapshot(
            code = code,
            title = title,
            attempts = attempts,
            dock = dock,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient scorecard snapshot in status draft. */
fun newLenientScorecardSnapshot(code: String, title: String): LenientScorecardSnapshot {
    return LenientScorecardSnapshot(
        code = code,
        title = title,
        attempts = 0,
        dock = "",
        fuelLiters = 0.0,
        status = LenientScorecardSnapshotStatus.DRAFT,
    )
}
