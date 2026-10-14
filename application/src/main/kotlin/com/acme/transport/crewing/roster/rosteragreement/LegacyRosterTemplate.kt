package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy roster template is in its life cycle. */
enum class LegacyRosterTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy roster template, with what the planners need to act on it. */
data class LegacyRosterTemplate(
    var code: String,
    var title: String,
    var priority: Int,
    var routeCode: String,
    var maximum: Double,
    var discount: Double,
    var trailerId: String,
    var graceDays: Int,
    var carrier: String,
    var attempts: Int,
    var dock: String,
    var status: LegacyRosterTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRosterTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRosterTemplateStatus): Boolean {
        when (status) {
            LegacyRosterTemplateStatus.REQUESTED -> return next == LegacyRosterTemplateStatus.REVOKED || next == LegacyRosterTemplateStatus.EXPIRED
            LegacyRosterTemplateStatus.GRANTED -> {
                when (next) {
                    LegacyRosterTemplateStatus.REVOKED, LegacyRosterTemplateStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyRosterTemplateStatus.REVOKED -> {
                when (next) {
                    LegacyRosterTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRosterTemplateStatus) {
        if (!canMoveTo(next)) {
            error("legacy roster template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((discount + priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyRosterTemplate {
        return LegacyRosterTemplate(
            code = code,
            title = title,
            priority = priority,
            routeCode = routeCode,
            maximum = maximum,
            discount = discount,
            trailerId = trailerId,
            graceDays = graceDays,
            carrier = carrier,
            attempts = attempts,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy roster template in status requested. */
fun newLegacyRosterTemplate(code: String, title: String): LegacyRosterTemplate {
    return LegacyRosterTemplate(
        code = code,
        title = title,
        priority = 0,
        routeCode = "",
        maximum = 0.0,
        discount = 0.0,
        trailerId = "",
        graceDays = 0,
        carrier = "",
        attempts = 0,
        dock = "",
        status = LegacyRosterTemplateStatus.REQUESTED,
    )
}
