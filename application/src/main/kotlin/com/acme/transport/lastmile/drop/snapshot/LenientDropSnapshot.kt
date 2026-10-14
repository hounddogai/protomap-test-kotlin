package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient drop snapshot records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a lenient drop snapshot is in its life cycle. */
enum class LenientDropSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient drop snapshot, with what the planners need to act on it. */
data class LenientDropSnapshot(
    var code: String,
    var title: String,
    var pallets: Int,
    var margin: Double,
    var loadKg: Double,
    var depot: String,
    var shiftCode: String,
    var segment: String,
    var durationMinutes: Int,
    var trailerId: String,
    var status: LenientDropSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropSnapshotStatus): Boolean {
        when (status) {
            LenientDropSnapshotStatus.DRAFT -> {
                when (next) {
                    LenientDropSnapshotStatus.ACTIVE, LenientDropSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientDropSnapshotStatus.PENDING -> return next == LenientDropSnapshotStatus.CLOSED
            LenientDropSnapshotStatus.ACTIVE -> return next == LenientDropSnapshotStatus.SUSPENDED || next == LenientDropSnapshotStatus.CLOSED
            LenientDropSnapshotStatus.SUSPENDED -> {
                when (next) {
                    LenientDropSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDropSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((durationMinutes.toDouble() - loadKg) * 100) / 100

    fun snapshot(): LenientDropSnapshot {
        return LenientDropSnapshot(
            code = code,
            title = title,
            pallets = pallets,
            margin = margin,
            loadKg = loadKg,
            depot = depot,
            shiftCode = shiftCode,
            segment = segment,
            durationMinutes = durationMinutes,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient drop snapshot in status draft. */
fun newLenientDropSnapshot(code: String, title: String): LenientDropSnapshot {
    return LenientDropSnapshot(
        code = code,
        title = title,
        pallets = 0,
        margin = 0.0,
        loadKg = 0.0,
        depot = "",
        shiftCode = "",
        segment = "",
        durationMinutes = 0,
        trailerId = "",
        status = LenientDropSnapshotStatus.DRAFT,
    )
}
