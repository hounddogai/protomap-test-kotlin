package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cross dock baseline is in its life cycle. */
enum class TieredCrossDockBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered cross dock baseline and its current status. */
data class TieredCrossDockBaseline(
    var code: String,
    var name: String,
    var archived: Boolean,
    var notes: String,
    var pallets: Int,
    var visible: Boolean,
    var startsAt: Int,
    var source: String,
    var status: TieredCrossDockBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockBaselineStatus): Boolean {
        when (status) {
            TieredCrossDockBaselineStatus.PLANNED -> return next == TieredCrossDockBaselineStatus.CANCELLED
            TieredCrossDockBaselineStatus.SCHEDULED -> {
                when (next) {
                    TieredCrossDockBaselineStatus.COMPLETED -> return true
                }
                return false
            }
            TieredCrossDockBaselineStatus.IN_PROGRESS -> return next == TieredCrossDockBaselineStatus.CANCELLED
            TieredCrossDockBaselineStatus.COMPLETED -> {
                when (next) {
                    TieredCrossDockBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCrossDockBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock baseline $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round(startsAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): TieredCrossDockBaseline {
        return TieredCrossDockBaseline(
            code = code,
            name = name,
            archived = archived,
            notes = notes,
            pallets = pallets,
            visible = visible,
            startsAt = startsAt,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered cross dock baseline in status planned. */
fun newTieredCrossDockBaseline(code: String, name: String): TieredCrossDockBaseline {
    return TieredCrossDockBaseline(
        code = code,
        name = name,
        archived = false,
        notes = "",
        pallets = 0,
        visible = false,
        startsAt = 0,
        source = "",
        status = TieredCrossDockBaselineStatus.PLANNED,
    )
}
