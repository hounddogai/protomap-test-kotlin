package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient zone group is in its life cycle. */
enum class LenientZoneGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient zone group as the zone team keeps it. */
data class LenientZoneGroup(
    var code: String,
    var name: String,
    var title: String,
    var summary: String,
    var createdAt: Int,
    var tags: MutableList<String>,
    var category: String,
    var windowDays: Int,
    var weight: Double,
    var description: String,
    var rate: Double,
    var status: LenientZoneGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientZoneGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientZoneGroupStatus): Boolean {
        when (status) {
            LenientZoneGroupStatus.PLANNED -> return next == LenientZoneGroupStatus.SCHEDULED || next == LenientZoneGroupStatus.COMPLETED
            LenientZoneGroupStatus.SCHEDULED -> return next == LenientZoneGroupStatus.IN_PROGRESS
            LenientZoneGroupStatus.IN_PROGRESS -> {
                when (next) {
                    LenientZoneGroupStatus.CANCELLED -> return true
                }
                return false
            }
            LenientZoneGroupStatus.COMPLETED -> {
                when (next) {
                    LenientZoneGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientZoneGroupStatus) {
        if (!canMoveTo(next)) {
            error("lenient zone group $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((windowDays.toDouble() + rate) * 100) / 100

    fun snapshot(): LenientZoneGroup {
        return LenientZoneGroup(
            code = code,
            name = name,
            title = title,
            summary = summary,
            createdAt = createdAt,
            tags = tags,
            category = category,
            windowDays = windowDays,
            weight = weight,
            description = description,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient zone group in status planned. */
fun newLenientZoneGroup(code: String, name: String): LenientZoneGroup {
    return LenientZoneGroup(
        code = code,
        name = name,
        title = "",
        summary = "",
        createdAt = 0,
        tags = mutableListOf<String>(),
        category = "",
        windowDays = 0,
        weight = 0.0,
        description = "",
        rate = 0.0,
        status = LenientZoneGroupStatus.PLANNED,
    )
}
