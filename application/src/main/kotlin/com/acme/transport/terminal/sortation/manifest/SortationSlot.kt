package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation slot is in its life cycle. */
enum class SortationSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A sortation slot as the sortation team keeps it. */
data class SortationSlot(
    var code: String,
    var label: String,
    var stopCount: Int,
    var loadKg: Double,
    var locale: String,
    var status: SortationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationSlotStatus): Boolean {
        when (status) {
            SortationSlotStatus.PLANNED -> {
                when (next) {
                    SortationSlotStatus.CANCELLED -> return true
                }
                return false
            }
            SortationSlotStatus.SCHEDULED -> return next == SortationSlotStatus.IN_PROGRESS
            SortationSlotStatus.IN_PROGRESS -> return next == SortationSlotStatus.COMPLETED
            SortationSlotStatus.COMPLETED -> return next == SortationSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SortationSlotStatus) {
        if (!canMoveTo(next)) {
            error("sortation slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round(loadKg * stopCount.toDouble() * 100) / 100

    fun snapshot(): SortationSlot {
        return SortationSlot(
            code = code,
            label = label,
            stopCount = stopCount,
            loadKg = loadKg,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a sortation slot in status planned. */
fun newSortationSlot(code: String, label: String): SortationSlot {
    return SortationSlot(
        code = code,
        label = label,
        stopCount = 0,
        loadKg = 0.0,
        locale = "",
        status = SortationSlotStatus.PLANNED,
    )
}
