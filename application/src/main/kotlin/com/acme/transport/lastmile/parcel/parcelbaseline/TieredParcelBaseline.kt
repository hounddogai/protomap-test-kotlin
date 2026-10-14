package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.text.normalize

// The tiered parcel baseline records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a tiered parcel baseline is in its life cycle. */
enum class TieredParcelBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered parcel baseline, with what the planners need to act on it. */
data class TieredParcelBaseline(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var trailerId: String,
    var shiftCode: String,
    var status: TieredParcelBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredParcelBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredParcelBaselineStatus): Boolean {
        when (status) {
            TieredParcelBaselineStatus.IDLE -> return next == TieredParcelBaselineStatus.STOPPED
            TieredParcelBaselineStatus.WARMING -> {
                when (next) {
                    TieredParcelBaselineStatus.STOPPED -> return true
                }
                return false
            }
            TieredParcelBaselineStatus.READY -> return next == TieredParcelBaselineStatus.DRAINING
            TieredParcelBaselineStatus.DRAINING -> return next == TieredParcelBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredParcelBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered parcel baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): TieredParcelBaseline {
        return TieredParcelBaseline(
            code = code,
            title = title,
            updatedAt = updatedAt,
            trailerId = trailerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered parcel baseline in status idle. */
fun newTieredParcelBaseline(code: String, title: String): TieredParcelBaseline {
    return TieredParcelBaseline(
        code = code,
        title = title,
        updatedAt = 0,
        trailerId = "",
        shiftCode = "",
        status = TieredParcelBaselineStatus.IDLE,
    )
}
