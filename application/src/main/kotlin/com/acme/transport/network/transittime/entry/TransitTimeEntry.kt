package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time entry records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time entry is in its life cycle. */
enum class TransitTimeEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a transit time entry and its current status. */
data class TransitTimeEntry(
    var code: String,
    var title: String,
    var startsAt: Int,
    var carrier: String,
    var driverId: String,
    var category: String,
    var batchSize: Int,
    var minimum: Double,
    var vehicleId: String,
    var loadKg: Double,
    var status: TransitTimeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeEntryStatus): Boolean {
        when (status) {
            TransitTimeEntryStatus.CREATED -> {
                when (next) {
                    TransitTimeEntryStatus.VALIDATED, TransitTimeEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            TransitTimeEntryStatus.VALIDATED -> {
                when (next) {
                    TransitTimeEntryStatus.PUBLISHED, TransitTimeEntryStatus.RETIRED -> return true
                }
                return false
            }
            TransitTimeEntryStatus.PUBLISHED -> {
                when (next) {
                    TransitTimeEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeEntryStatus) {
        if (!canMoveTo(next)) {
            error("transit time entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((loadKg - startsAt.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeEntry {
        return TransitTimeEntry(
            code = code,
            title = title,
            startsAt = startsAt,
            carrier = carrier,
            driverId = driverId,
            category = category,
            batchSize = batchSize,
            minimum = minimum,
            vehicleId = vehicleId,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time entry in status created. */
fun newTransitTimeEntry(code: String, title: String): TransitTimeEntry {
    return TransitTimeEntry(
        code = code,
        title = title,
        startsAt = 0,
        carrier = "",
        driverId = "",
        category = "",
        batchSize = 0,
        minimum = 0.0,
        vehicleId = "",
        loadKg = 0.0,
        status = TransitTimeEntryStatus.CREATED,
    )
}
