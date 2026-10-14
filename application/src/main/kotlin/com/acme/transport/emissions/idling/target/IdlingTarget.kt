package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling target records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling target is in its life cycle. */
enum class IdlingTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling target as the idling team keeps it. */
data class IdlingTarget(
    var code: String,
    var title: String,
    var archived: Boolean,
    var priority: Int,
    var rate: Double,
    var weight: Double,
    var lane: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var graceDays: Int,
    var channel: String,
    var status: IdlingTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingTargetStatus): Boolean {
        when (status) {
            IdlingTargetStatus.CREATED -> return next == IdlingTargetStatus.RETIRED
            IdlingTargetStatus.VALIDATED -> {
                when (next) {
                    IdlingTargetStatus.RETIRED -> return true
                }
                return false
            }
            IdlingTargetStatus.PUBLISHED -> {
                when (next) {
                    IdlingTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingTargetStatus) {
        if (!canMoveTo(next)) {
            error("idling target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round(priority.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): IdlingTarget {
        return IdlingTarget(
            code = code,
            title = title,
            archived = archived,
            priority = priority,
            rate = rate,
            weight = weight,
            lane = lane,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            graceDays = graceDays,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a idling target in status created. */
fun newIdlingTarget(code: String, title: String): IdlingTarget {
    return IdlingTarget(
        code = code,
        title = title,
        archived = false,
        priority = 0,
        rate = 0.0,
        weight = 0.0,
        lane = "",
        durationMinutes = 0,
        loadKg = 0.0,
        graceDays = 0,
        channel = "",
        status = IdlingTargetStatus.CREATED,
    )
}
