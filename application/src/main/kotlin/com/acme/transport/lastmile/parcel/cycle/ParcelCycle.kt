package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel cycle records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel cycle is in its life cycle. */
enum class ParcelCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a parcel cycle and its current status. */
data class ParcelCycle(
    var code: String,
    var title: String,
    var ownerId: String,
    var stopCount: Int,
    var graceDays: Int,
    var status: ParcelCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelCycleStatus): Boolean {
        when (status) {
            ParcelCycleStatus.CREATED -> {
                when (next) {
                    ParcelCycleStatus.VALIDATED, ParcelCycleStatus.RETIRED -> return true
                }
                return false
            }
            ParcelCycleStatus.VALIDATED -> {
                when (next) {
                    ParcelCycleStatus.PUBLISHED, ParcelCycleStatus.RETIRED -> return true
                }
                return false
            }
            ParcelCycleStatus.PUBLISHED -> {
                when (next) {
                    ParcelCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelCycleStatus) {
        if (!canMoveTo(next)) {
            error("parcel cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round(stopCount.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): ParcelCycle {
        return ParcelCycle(
            code = code,
            title = title,
            ownerId = ownerId,
            stopCount = stopCount,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a parcel cycle in status created. */
fun newParcelCycle(code: String, title: String): ParcelCycle {
    return ParcelCycle(
        code = code,
        title = title,
        ownerId = "",
        stopCount = 0,
        graceDays = 0,
        status = ParcelCycleStatus.CREATED,
    )
}
