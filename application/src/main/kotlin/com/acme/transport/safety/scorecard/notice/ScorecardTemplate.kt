package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard template is in its life cycle. */
enum class ScorecardTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scorecard template, with what the planners need to act on it. */
data class ScorecardTemplate(
    var code: String,
    var title: String,
    var priority: Int,
    var approved: Boolean,
    var weight: Double,
    var etaMinutes: Int,
    var startsAt: Int,
    var notes: String,
    var status: ScorecardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardTemplateStatus): Boolean {
        when (status) {
            ScorecardTemplateStatus.CREATED -> {
                when (next) {
                    ScorecardTemplateStatus.VALIDATED -> return true
                }
                return false
            }
            ScorecardTemplateStatus.VALIDATED -> return next == ScorecardTemplateStatus.PUBLISHED
            ScorecardTemplateStatus.PUBLISHED -> return next == ScorecardTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScorecardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scorecard template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round(weight * priority.toDouble() * 100) / 100

    fun snapshot(): ScorecardTemplate {
        return ScorecardTemplate(
            code = code,
            title = title,
            priority = priority,
            approved = approved,
            weight = weight,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scorecard template in status created. */
fun newScorecardTemplate(code: String, title: String): ScorecardTemplate {
    return ScorecardTemplate(
        code = code,
        title = title,
        priority = 0,
        approved = false,
        weight = 0.0,
        etaMinutes = 0,
        startsAt = 0,
        notes = "",
        status = ScorecardTemplateStatus.CREATED,
    )
}
