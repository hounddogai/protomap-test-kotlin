package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary parcel cycle records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a primary parcel cycle is in its life cycle. */
enum class PrimaryParcelCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary parcel cycle, with what the planners need to act on it. */
data class PrimaryParcelCycle(
    var code: String,
    var name: String,
    var active: Boolean,
    var createdAt: Int,
    var title: String,
    var pallets: Int,
    var attempts: Int,
    var limit: Int,
    var carrier: String,
    var status: PrimaryParcelCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryParcelCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryParcelCycleStatus): Boolean {
        when (status) {
            PrimaryParcelCycleStatus.IDLE -> {
                when (next) {
                    PrimaryParcelCycleStatus.WARMING, PrimaryParcelCycleStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryParcelCycleStatus.WARMING -> return next == PrimaryParcelCycleStatus.READY || next == PrimaryParcelCycleStatus.STOPPED
            PrimaryParcelCycleStatus.READY -> return next == PrimaryParcelCycleStatus.DRAINING
            PrimaryParcelCycleStatus.DRAINING -> {
                when (next) {
                    PrimaryParcelCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryParcelCycleStatus) {
        if (!canMoveTo(next)) {
            error("primary parcel cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round((createdAt.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): PrimaryParcelCycle {
        return PrimaryParcelCycle(
            code = code,
            name = name,
            active = active,
            createdAt = createdAt,
            title = title,
            pallets = pallets,
            attempts = attempts,
            limit = limit,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary parcel cycle in status idle. */
fun newPrimaryParcelCycle(code: String, name: String): PrimaryParcelCycle {
    return PrimaryParcelCycle(
        code = code,
        name = name,
        active = false,
        createdAt = 0,
        title = "",
        pallets = 0,
        attempts = 0,
        limit = 0,
        carrier = "",
        status = PrimaryParcelCycleStatus.IDLE,
    )
}
