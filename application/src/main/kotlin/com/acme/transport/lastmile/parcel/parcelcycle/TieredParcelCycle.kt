package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered parcel cycle records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a tiered parcel cycle is in its life cycle. */
enum class TieredParcelCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered parcel cycle as the parcel team keeps it. */
data class TieredParcelCycle(
    var code: String,
    var title: String,
    var source: String,
    var contactEmail: String,
    var windowDays: Int,
    var durationMinutes: Int,
    var sequence: Int,
    var status: TieredParcelCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredParcelCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredParcelCycleStatus): Boolean {
        when (status) {
            TieredParcelCycleStatus.NEW -> return next == TieredParcelCycleStatus.RUNNING || next == TieredParcelCycleStatus.DONE
            TieredParcelCycleStatus.QUEUED -> return next == TieredParcelCycleStatus.FAILED
            TieredParcelCycleStatus.RUNNING -> {
                when (next) {
                    TieredParcelCycleStatus.DONE, TieredParcelCycleStatus.FAILED -> return true
                }
                return false
            }
            TieredParcelCycleStatus.DONE -> {
                when (next) {
                    TieredParcelCycleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredParcelCycleStatus) {
        if (!canMoveTo(next)) {
            error("tiered parcel cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round((windowDays.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): TieredParcelCycle {
        return TieredParcelCycle(
            code = code,
            title = title,
            source = source,
            contactEmail = contactEmail,
            windowDays = windowDays,
            durationMinutes = durationMinutes,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered parcel cycle in status new. */
fun newTieredParcelCycle(code: String, title: String): TieredParcelCycle {
    return TieredParcelCycle(
        code = code,
        title = title,
        source = "",
        contactEmail = "",
        windowDays = 0,
        durationMinutes = 0,
        sequence = 0,
        status = TieredParcelCycleStatus.NEW,
    )
}
