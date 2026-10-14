package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending idling entry is in its life cycle. */
enum class PendingIdlingEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending idling entry as the idling team keeps it. */
data class PendingIdlingEntry(
    var code: String,
    var label: String,
    var threshold: Double,
    var minimum: Double,
    var version: Int,
    var driverId: String,
    var limit: Int,
    var status: PendingIdlingEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingEntryStatus): Boolean {
        when (status) {
            PendingIdlingEntryStatus.CREATED -> return next == PendingIdlingEntryStatus.PUBLISHED || next == PendingIdlingEntryStatus.RETIRED
            PendingIdlingEntryStatus.VALIDATED -> return next == PendingIdlingEntryStatus.PUBLISHED || next == PendingIdlingEntryStatus.RETIRED
            PendingIdlingEntryStatus.PUBLISHED -> {
                when (next) {
                    PendingIdlingEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingIdlingEntryStatus) {
        if (!canMoveTo(next)) {
            error("pending idling entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((version.toDouble() + minimum) * 100) / 100

    fun snapshot(): PendingIdlingEntry {
        return PendingIdlingEntry(
            code = code,
            label = label,
            threshold = threshold,
            minimum = minimum,
            version = version,
            driverId = driverId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending idling entry in status created. */
fun newPendingIdlingEntry(code: String, label: String): PendingIdlingEntry {
    return PendingIdlingEntry(
        code = code,
        label = label,
        threshold = 0.0,
        minimum = 0.0,
        version = 0,
        driverId = "",
        limit = 0,
        status = PendingIdlingEntryStatus.CREATED,
    )
}
