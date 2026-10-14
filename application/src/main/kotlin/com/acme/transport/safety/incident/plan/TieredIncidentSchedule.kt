package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered incident schedule is in its life cycle. */
enum class TieredIncidentScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered incident schedule. Its code is unique within a tenant. */
data class TieredIncidentSchedule(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var channel: String,
    var maximum: Double,
    var capacity: Int,
    var threshold: Double,
    var contactEmail: String,
    var approved: Boolean,
    var status: TieredIncidentScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIncidentScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIncidentScheduleStatus): Boolean {
        when (status) {
            TieredIncidentScheduleStatus.OPEN -> return next == TieredIncidentScheduleStatus.ARCHIVED
            TieredIncidentScheduleStatus.IN_REVIEW -> {
                when (next) {
                    TieredIncidentScheduleStatus.RESOLVED, TieredIncidentScheduleStatus.REOPENED -> return true
                }
                return false
            }
            TieredIncidentScheduleStatus.RESOLVED -> {
                when (next) {
                    TieredIncidentScheduleStatus.REOPENED, TieredIncidentScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredIncidentScheduleStatus.REOPENED -> return next == TieredIncidentScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredIncidentScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tiered incident schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(threshold * maximum * 100) / 100

    fun snapshot(): TieredIncidentSchedule {
        return TieredIncidentSchedule(
            code = code,
            label = label,
            pinned = pinned,
            tags = tags,
            channel = channel,
            maximum = maximum,
            capacity = capacity,
            threshold = threshold,
            contactEmail = contactEmail,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered incident schedule in status open. */
fun newTieredIncidentSchedule(code: String, label: String): TieredIncidentSchedule {
    return TieredIncidentSchedule(
        code = code,
        label = label,
        pinned = false,
        tags = mutableListOf<String>(),
        channel = "",
        maximum = 0.0,
        capacity = 0,
        threshold = 0.0,
        contactEmail = "",
        approved = false,
        status = TieredIncidentScheduleStatus.OPEN,
    )
}
