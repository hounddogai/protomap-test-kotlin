package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation mapping is in its life cycle. */
enum class SortationMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A sortation mapping as the sortation team keeps it. */
data class SortationMapping(
    var code: String,
    var title: String,
    var phone: String,
    var reviewer: String,
    var notes: String,
    var cost: Double,
    var unit: String,
    var timezone: String,
    var country: String,
    var margin: Double,
    var tags: MutableList<String>,
    var status: SortationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationMappingStatus): Boolean {
        when (status) {
            SortationMappingStatus.IDLE -> {
                when (next) {
                    SortationMappingStatus.DRAINING, SortationMappingStatus.STOPPED -> return true
                }
                return false
            }
            SortationMappingStatus.WARMING -> {
                when (next) {
                    SortationMappingStatus.READY, SortationMappingStatus.DRAINING -> return true
                }
                return false
            }
            SortationMappingStatus.READY -> {
                when (next) {
                    SortationMappingStatus.DRAINING, SortationMappingStatus.STOPPED -> return true
                }
                return false
            }
            SortationMappingStatus.DRAINING -> {
                when (next) {
                    SortationMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationMappingStatus) {
        if (!canMoveTo(next)) {
            error("sortation mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((cost + margin) * 100) / 100

    fun snapshot(): SortationMapping {
        return SortationMapping(
            code = code,
            title = title,
            phone = phone,
            reviewer = reviewer,
            notes = notes,
            cost = cost,
            unit = unit,
            timezone = timezone,
            country = country,
            margin = margin,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a sortation mapping in status idle. */
fun newSortationMapping(code: String, title: String): SortationMapping {
    return SortationMapping(
        code = code,
        title = title,
        phone = "",
        reviewer = "",
        notes = "",
        cost = 0.0,
        unit = "",
        timezone = "",
        country = "",
        margin = 0.0,
        tags = mutableListOf<String>(),
        status = SortationMappingStatus.IDLE,
    )
}
