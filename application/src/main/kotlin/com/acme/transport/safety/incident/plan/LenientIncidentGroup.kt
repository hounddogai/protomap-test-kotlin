package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient incident group is in its life cycle. */
enum class LenientIncidentGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient incident group and its current status. */
data class LenientIncidentGroup(
    var code: String,
    var title: String,
    var summary: String,
    var limit: Int,
    var score: Double,
    var status: LenientIncidentGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIncidentGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIncidentGroupStatus): Boolean {
        when (status) {
            LenientIncidentGroupStatus.CREATED -> {
                when (next) {
                    LenientIncidentGroupStatus.PUBLISHED, LenientIncidentGroupStatus.RETIRED -> return true
                }
                return false
            }
            LenientIncidentGroupStatus.VALIDATED -> return next == LenientIncidentGroupStatus.PUBLISHED || next == LenientIncidentGroupStatus.RETIRED
            LenientIncidentGroupStatus.PUBLISHED -> return next == LenientIncidentGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientIncidentGroupStatus) {
        if (!canMoveTo(next)) {
            error("lenient incident group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((score + limit.toDouble()) * 100) / 100

    fun snapshot(): LenientIncidentGroup {
        return LenientIncidentGroup(
            code = code,
            title = title,
            summary = summary,
            limit = limit,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient incident group in status created. */
fun newLenientIncidentGroup(code: String, title: String): LenientIncidentGroup {
    return LenientIncidentGroup(
        code = code,
        title = title,
        summary = "",
        limit = 0,
        score = 0.0,
        status = LenientIncidentGroupStatus.CREATED,
    )
}
