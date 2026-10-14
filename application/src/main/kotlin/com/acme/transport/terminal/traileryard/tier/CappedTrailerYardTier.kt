package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped trailer yard tier records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a capped trailer yard tier is in its life cycle. */
enum class CappedTrailerYardTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped trailer yard tier as the trailer yard team keeps it. */
data class CappedTrailerYardTier(
    var code: String,
    var title: String,
    var ceiling: Double,
    var windowDays: Int,
    var pallets: Int,
    var notes: String,
    var status: CappedTrailerYardTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerYardTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerYardTierStatus): Boolean {
        when (status) {
            CappedTrailerYardTierStatus.IDLE -> return next == CappedTrailerYardTierStatus.WARMING
            CappedTrailerYardTierStatus.WARMING -> return next == CappedTrailerYardTierStatus.READY
            CappedTrailerYardTierStatus.READY -> {
                when (next) {
                    CappedTrailerYardTierStatus.DRAINING, CappedTrailerYardTierStatus.STOPPED -> return true
                }
                return false
            }
            CappedTrailerYardTierStatus.DRAINING -> return next == CappedTrailerYardTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedTrailerYardTierStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer yard tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedVolume(): Double = round((pallets.toDouble() - ceiling) * 100) / 100

    fun snapshot(): CappedTrailerYardTier {
        return CappedTrailerYardTier(
            code = code,
            title = title,
            ceiling = ceiling,
            windowDays = windowDays,
            pallets = pallets,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped trailer yard tier in status idle. */
fun newCappedTrailerYardTier(code: String, title: String): CappedTrailerYardTier {
    return CappedTrailerYardTier(
        code = code,
        title = title,
        ceiling = 0.0,
        windowDays = 0,
        pallets = 0,
        notes = "",
        status = CappedTrailerYardTierStatus.IDLE,
    )
}
