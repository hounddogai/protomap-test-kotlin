package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation group records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation group is in its life cycle. */
enum class SortationGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One sortation group, with what the planners need to act on it. */
data class SortationGroup(
    var code: String,
    var title: String,
    var batchSize: Int,
    var capacity: Int,
    var cost: Double,
    var unit: String,
    var segment: String,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var margin: Double,
    var status: SortationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationGroupStatus): Boolean {
        when (status) {
            SortationGroupStatus.PLANNED -> return next == SortationGroupStatus.COMPLETED
            SortationGroupStatus.SCHEDULED -> return next == SortationGroupStatus.COMPLETED
            SortationGroupStatus.IN_PROGRESS -> return next == SortationGroupStatus.COMPLETED
            SortationGroupStatus.COMPLETED -> {
                when (next) {
                    SortationGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationGroupStatus) {
        if (!canMoveTo(next)) {
            error("sortation group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((batchSize.toDouble() + margin) * 100) / 100

    fun snapshot(): SortationGroup {
        return SortationGroup(
            code = code,
            title = title,
            batchSize = batchSize,
            capacity = capacity,
            cost = cost,
            unit = unit,
            segment = segment,
            trailerId = trailerId,
            attributes = attributes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a sortation group in status planned. */
fun newSortationGroup(code: String, title: String): SortationGroup {
    return SortationGroup(
        code = code,
        title = title,
        batchSize = 0,
        capacity = 0,
        cost = 0.0,
        unit = "",
        segment = "",
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        margin = 0.0,
        status = SortationGroupStatus.PLANNED,
    )
}
