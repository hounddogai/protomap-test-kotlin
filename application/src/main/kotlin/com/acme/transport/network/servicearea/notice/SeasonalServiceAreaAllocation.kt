package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal service area allocation is in its life cycle. */
enum class SeasonalServiceAreaAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal service area allocation, with what the planners need to act on it. */
data class SeasonalServiceAreaAllocation(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var description: String,
    var label: String,
    var summary: String,
    var sequence: Int,
    var startsAt: Int,
    var limit: Int,
    var status: SeasonalServiceAreaAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServiceAreaAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServiceAreaAllocationStatus): Boolean {
        when (status) {
            SeasonalServiceAreaAllocationStatus.PLANNED -> {
                when (next) {
                    SeasonalServiceAreaAllocationStatus.COMPLETED, SeasonalServiceAreaAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalServiceAreaAllocationStatus.SCHEDULED -> return next == SeasonalServiceAreaAllocationStatus.IN_PROGRESS
            SeasonalServiceAreaAllocationStatus.IN_PROGRESS -> return next == SeasonalServiceAreaAllocationStatus.COMPLETED || next == SeasonalServiceAreaAllocationStatus.CANCELLED
            SeasonalServiceAreaAllocationStatus.COMPLETED -> return next == SeasonalServiceAreaAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalServiceAreaAllocationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service area allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"seasonal service area allocation"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((limit.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalServiceAreaAllocation {
        return SeasonalServiceAreaAllocation(
            code = code,
            title = title,
            attributes = attributes,
            description = description,
            label = label,
            summary = summary,
            sequence = sequence,
            startsAt = startsAt,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal service area allocation in status planned. */
fun newSeasonalServiceAreaAllocation(code: String, title: String): SeasonalServiceAreaAllocation {
    return SeasonalServiceAreaAllocation(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        description = "",
        label = "",
        summary = "",
        sequence = 0,
        startsAt = 0,
        limit = 0,
        status = SeasonalServiceAreaAllocationStatus.PLANNED,
    )
}
