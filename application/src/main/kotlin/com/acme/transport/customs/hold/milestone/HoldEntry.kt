package com.acme.transport.customs.hold.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold entry is in its life cycle. */
enum class HoldEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a hold entry and its current status. */
data class HoldEntry(
    var code: String,
    var label: String,
    var stopCount: Int,
    var ownerId: String,
    var country: String,
    var priority: Int,
    var region: String,
    var status: HoldEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldEntryStatus): Boolean {
        when (status) {
            HoldEntryStatus.PROPOSED -> return next == HoldEntryStatus.APPROVED || next == HoldEntryStatus.WITHDRAWN
            HoldEntryStatus.APPROVED -> return next == HoldEntryStatus.WITHDRAWN
            HoldEntryStatus.REJECTED -> {
                when (next) {
                    HoldEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldEntryStatus) {
        if (!canMoveTo(next)) {
            error("hold entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((stopCount.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): HoldEntry {
        return HoldEntry(
            code = code,
            label = label,
            stopCount = stopCount,
            ownerId = ownerId,
            country = country,
            priority = priority,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a hold entry in status proposed. */
fun newHoldEntry(code: String, label: String): HoldEntry {
    return HoldEntry(
        code = code,
        label = label,
        stopCount = 0,
        ownerId = "",
        country = "",
        priority = 0,
        region = "",
        status = HoldEntryStatus.PROPOSED,
    )
}
