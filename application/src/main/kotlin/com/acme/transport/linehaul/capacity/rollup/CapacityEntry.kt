package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity entry is in its life cycle. */
enum class CapacityEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capacity entry as the capacity team keeps it. */
data class CapacityEntry(
    var code: String,
    var title: String,
    var driverId: String,
    var ceiling: Double,
    var category: String,
    var quantity: Int,
    var windowDays: Int,
    var status: CapacityEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityEntryStatus): Boolean {
        when (status) {
            CapacityEntryStatus.REQUESTED -> {
                when (next) {
                    CapacityEntryStatus.GRANTED, CapacityEntryStatus.REVOKED -> return true
                }
                return false
            }
            CapacityEntryStatus.GRANTED -> return next == CapacityEntryStatus.EXPIRED
            CapacityEntryStatus.REVOKED -> return next == CapacityEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CapacityEntryStatus) {
        if (!canMoveTo(next)) {
            error("capacity entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((ceiling + windowDays.toDouble()) * 100) / 100

    fun snapshot(): CapacityEntry {
        return CapacityEntry(
            code = code,
            title = title,
            driverId = driverId,
            ceiling = ceiling,
            category = category,
            quantity = quantity,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capacity entry in status requested. */
fun newCapacityEntry(code: String, title: String): CapacityEntry {
    return CapacityEntry(
        code = code,
        title = title,
        driverId = "",
        ceiling = 0.0,
        category = "",
        quantity = 0,
        windowDays = 0,
        status = CapacityEntryStatus.REQUESTED,
    )
}
