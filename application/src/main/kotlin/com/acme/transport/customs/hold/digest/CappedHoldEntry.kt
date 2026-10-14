package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hold entry is in its life cycle. */
enum class CappedHoldEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped hold entry and its current status. */
data class CappedHoldEntry(
    var code: String,
    var title: String,
    var region: String,
    var threshold: Double,
    var pallets: Int,
    var notes: String,
    var routeCode: String,
    var timezone: String,
    var lane: String,
    var status: CappedHoldEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHoldEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHoldEntryStatus): Boolean {
        when (status) {
            CappedHoldEntryStatus.CREATED -> return next == CappedHoldEntryStatus.VALIDATED
            CappedHoldEntryStatus.VALIDATED -> {
                when (next) {
                    CappedHoldEntryStatus.PUBLISHED, CappedHoldEntryStatus.RETIRED -> return true
                }
                return false
            }
            CappedHoldEntryStatus.PUBLISHED -> return next == CappedHoldEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedHoldEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped hold entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round(threshold * pallets.toDouble() * 100) / 100

    fun snapshot(): CappedHoldEntry {
        return CappedHoldEntry(
            code = code,
            title = title,
            region = region,
            threshold = threshold,
            pallets = pallets,
            notes = notes,
            routeCode = routeCode,
            timezone = timezone,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped hold entry in status created. */
fun newCappedHoldEntry(code: String, title: String): CappedHoldEntry {
    return CappedHoldEntry(
        code = code,
        title = title,
        region = "",
        threshold = 0.0,
        pallets = 0,
        notes = "",
        routeCode = "",
        timezone = "",
        lane = "",
        status = CappedHoldEntryStatus.CREATED,
    )
}
