package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered drop target is in its life cycle. */
enum class TieredDropTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tiered drop target, with what the planners need to act on it. */
data class TieredDropTarget(
    var code: String,
    var title: String,
    var segment: String,
    var expiresAt: Int,
    var notes: String,
    var durationMinutes: Int,
    var checksum: String,
    var currency: String,
    var status: TieredDropTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDropTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDropTargetStatus): Boolean {
        when (status) {
            TieredDropTargetStatus.DRAFT -> return next == TieredDropTargetStatus.ACTIVE
            TieredDropTargetStatus.PENDING -> return next == TieredDropTargetStatus.ACTIVE || next == TieredDropTargetStatus.CLOSED
            TieredDropTargetStatus.ACTIVE -> return next == TieredDropTargetStatus.SUSPENDED || next == TieredDropTargetStatus.CLOSED
            TieredDropTargetStatus.SUSPENDED -> {
                when (next) {
                    TieredDropTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDropTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered drop target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((expiresAt.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredDropTarget {
        return TieredDropTarget(
            code = code,
            title = title,
            segment = segment,
            expiresAt = expiresAt,
            notes = notes,
            durationMinutes = durationMinutes,
            checksum = checksum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered drop target in status draft. */
fun newTieredDropTarget(code: String, title: String): TieredDropTarget {
    return TieredDropTarget(
        code = code,
        title = title,
        segment = "",
        expiresAt = 0,
        notes = "",
        durationMinutes = 0,
        checksum = "",
        currency = "",
        status = TieredDropTargetStatus.DRAFT,
    )
}
