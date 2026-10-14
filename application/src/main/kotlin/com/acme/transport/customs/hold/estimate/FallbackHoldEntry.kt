package com.acme.transport.customs.hold.estimate

import com.acme.transport.kit.text.normalize

/** Where a fallback hold entry is in its life cycle. */
enum class FallbackHoldEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback hold entry and its current status. */
data class FallbackHoldEntry(
    var code: String,
    var title: String,
    var driverId: String,
    var pinned: Boolean,
    var category: String,
    var region: String,
    var retries: Int,
    var status: FallbackHoldEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackHoldEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackHoldEntryStatus): Boolean {
        when (status) {
            FallbackHoldEntryStatus.CREATED -> return next == FallbackHoldEntryStatus.RETIRED
            FallbackHoldEntryStatus.VALIDATED -> return next == FallbackHoldEntryStatus.PUBLISHED || next == FallbackHoldEntryStatus.RETIRED
            FallbackHoldEntryStatus.PUBLISHED -> {
                when (next) {
                    FallbackHoldEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackHoldEntryStatus) {
        if (!canMoveTo(next)) {
            error("fallback hold entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): FallbackHoldEntry {
        return FallbackHoldEntry(
            code = code,
            title = title,
            driverId = driverId,
            pinned = pinned,
            category = category,
            region = region,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback hold entry in status created. */
fun newFallbackHoldEntry(code: String, title: String): FallbackHoldEntry {
    return FallbackHoldEntry(
        code = code,
        title = title,
        driverId = "",
        pinned = false,
        category = "",
        region = "",
        retries = 0,
        status = FallbackHoldEntryStatus.CREATED,
    )
}
