package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation baseline records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation baseline is in its life cycle. */
enum class SortationBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A sortation baseline as the sortation team keeps it. */
data class SortationBaseline(
    var code: String,
    var name: String,
    var weight: Double,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var floorValue: Double,
    var margin: Double,
    var batchSize: Int,
    var status: SortationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationBaselineStatus): Boolean {
        when (status) {
            SortationBaselineStatus.DRAFT -> return next == SortationBaselineStatus.CLOSED
            SortationBaselineStatus.PENDING -> return next == SortationBaselineStatus.ACTIVE
            SortationBaselineStatus.ACTIVE -> return next == SortationBaselineStatus.SUSPENDED || next == SortationBaselineStatus.CLOSED
            SortationBaselineStatus.SUSPENDED -> {
                when (next) {
                    SortationBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("sortation baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((margin - batchSize.toDouble()) * 100) / 100

    fun snapshot(): SortationBaseline {
        return SortationBaseline(
            code = code,
            name = name,
            weight = weight,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            margin = margin,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a sortation baseline in status draft. */
fun newSortationBaseline(code: String, name: String): SortationBaseline {
    return SortationBaseline(
        code = code,
        name = name,
        weight = 0.0,
        fuelLiters = 0.0,
        durationMinutes = 0,
        floorValue = 0.0,
        margin = 0.0,
        batchSize = 0,
        status = SortationBaselineStatus.DRAFT,
    )
}
