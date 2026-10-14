package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled roster template records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a scheduled roster template is in its life cycle. */
enum class ScheduledRosterTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scheduled roster template, with what the planners need to act on it. */
data class ScheduledRosterTemplate(
    var code: String,
    var name: String,
    var driverId: String,
    var etaMinutes: Int,
    var ratio: Double,
    var sequence: Int,
    var segment: String,
    var updatedAt: Int,
    var archived: Boolean,
    var capacity: Int,
    var distanceKm: Double,
    var status: ScheduledRosterTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterTemplateStatus): Boolean {
        when (status) {
            ScheduledRosterTemplateStatus.PROPOSED -> return next == ScheduledRosterTemplateStatus.WITHDRAWN
            ScheduledRosterTemplateStatus.APPROVED -> {
                when (next) {
                    ScheduledRosterTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledRosterTemplateStatus.REJECTED -> {
                when (next) {
                    ScheduledRosterTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRosterTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round(ratio * sequence.toDouble() * 100) / 100

    fun snapshot(): ScheduledRosterTemplate {
        return ScheduledRosterTemplate(
            code = code,
            name = name,
            driverId = driverId,
            etaMinutes = etaMinutes,
            ratio = ratio,
            sequence = sequence,
            segment = segment,
            updatedAt = updatedAt,
            archived = archived,
            capacity = capacity,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled roster template in status proposed. */
fun newScheduledRosterTemplate(code: String, name: String): ScheduledRosterTemplate {
    return ScheduledRosterTemplate(
        code = code,
        name = name,
        driverId = "",
        etaMinutes = 0,
        ratio = 0.0,
        sequence = 0,
        segment = "",
        updatedAt = 0,
        archived = false,
        capacity = 0,
        distanceKm = 0.0,
        status = ScheduledRosterTemplateStatus.PROPOSED,
    )
}
