package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tire entry is in its life cycle. */
enum class TieredTireEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered tire entry. Its code is unique within a tenant. */
data class TieredTireEntry(
    var code: String,
    var title: String,
    var sequence: Int,
    var pallets: Int,
    var shiftCode: String,
    var priority: Int,
    var expiresAt: Int,
    var durationMinutes: Int,
    var status: TieredTireEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTireEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTireEntryStatus): Boolean {
        when (status) {
            TieredTireEntryStatus.OPEN -> return next == TieredTireEntryStatus.REOPENED
            TieredTireEntryStatus.IN_REVIEW -> {
                when (next) {
                    TieredTireEntryStatus.REOPENED -> return true
                }
                return false
            }
            TieredTireEntryStatus.RESOLVED -> return next == TieredTireEntryStatus.REOPENED || next == TieredTireEntryStatus.ARCHIVED
            TieredTireEntryStatus.REOPENED -> {
                when (next) {
                    TieredTireEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTireEntryStatus) {
        if (!canMoveTo(next)) {
            error("tiered tire entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round(pallets.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): TieredTireEntry {
        return TieredTireEntry(
            code = code,
            title = title,
            sequence = sequence,
            pallets = pallets,
            shiftCode = shiftCode,
            priority = priority,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered tire entry in status open. */
fun newTieredTireEntry(code: String, title: String): TieredTireEntry {
    return TieredTireEntry(
        code = code,
        title = title,
        sequence = 0,
        pallets = 0,
        shiftCode = "",
        priority = 0,
        expiresAt = 0,
        durationMinutes = 0,
        status = TieredTireEntryStatus.OPEN,
    )
}
