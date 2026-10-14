package com.acme.transport.lastmile.drop.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop draft records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop draft is in its life cycle. */
enum class DropDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a drop draft and its current status. */
data class DropDraft(
    var code: String,
    var label: String,
    var pallets: Int,
    var depot: String,
    var stopCount: Int,
    var tags: MutableList<String>,
    var driverId: String,
    var odometer: Int,
    var fuelLiters: Double,
    var timezone: String,
    var status: DropDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropDraftStatus): Boolean {
        when (status) {
            DropDraftStatus.IDLE -> return next == DropDraftStatus.READY || next == DropDraftStatus.DRAINING
            DropDraftStatus.WARMING -> return next == DropDraftStatus.DRAINING || next == DropDraftStatus.STOPPED
            DropDraftStatus.READY -> return next == DropDraftStatus.DRAINING || next == DropDraftStatus.STOPPED
            DropDraftStatus.DRAINING -> return next == DropDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DropDraftStatus) {
        if (!canMoveTo(next)) {
            error("drop draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((fuelLiters - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DropDraft {
        return DropDraft(
            code = code,
            label = label,
            pallets = pallets,
            depot = depot,
            stopCount = stopCount,
            tags = tags,
            driverId = driverId,
            odometer = odometer,
            fuelLiters = fuelLiters,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a drop draft in status idle. */
fun newDropDraft(code: String, label: String): DropDraft {
    return DropDraft(
        code = code,
        label = label,
        pallets = 0,
        depot = "",
        stopCount = 0,
        tags = mutableListOf<String>(),
        driverId = "",
        odometer = 0,
        fuelLiters = 0.0,
        timezone = "",
        status = DropDraftStatus.IDLE,
    )
}
