package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled roster override is in its life cycle. */
enum class ScheduledRosterOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scheduled roster override, with what the planners need to act on it. */
data class ScheduledRosterOverride(
    var code: String,
    var name: String,
    var priority: Int,
    var discount: Double,
    var amount: Double,
    var summary: String,
    var revision: Int,
    var category: String,
    var sequence: Int,
    var channel: String,
    var startsAt: Int,
    var status: ScheduledRosterOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterOverrideStatus): Boolean {
        when (status) {
            ScheduledRosterOverrideStatus.REQUESTED -> return next == ScheduledRosterOverrideStatus.GRANTED || next == ScheduledRosterOverrideStatus.REVOKED
            ScheduledRosterOverrideStatus.GRANTED -> {
                when (next) {
                    ScheduledRosterOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledRosterOverrideStatus.REVOKED -> {
                when (next) {
                    ScheduledRosterOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRosterOverrideStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((revision.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRosterOverride {
        return ScheduledRosterOverride(
            code = code,
            name = name,
            priority = priority,
            discount = discount,
            amount = amount,
            summary = summary,
            revision = revision,
            category = category,
            sequence = sequence,
            channel = channel,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled roster override in status requested. */
fun newScheduledRosterOverride(code: String, name: String): ScheduledRosterOverride {
    return ScheduledRosterOverride(
        code = code,
        name = name,
        priority = 0,
        discount = 0.0,
        amount = 0.0,
        summary = "",
        revision = 0,
        category = "",
        sequence = 0,
        channel = "",
        startsAt = 0,
        status = ScheduledRosterOverrideStatus.REQUESTED,
    )
}
