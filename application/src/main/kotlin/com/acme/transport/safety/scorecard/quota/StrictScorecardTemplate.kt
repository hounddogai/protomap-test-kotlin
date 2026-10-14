package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict scorecard template is in its life cycle. */
enum class StrictScorecardTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict scorecard template as the scorecard team keeps it. */
data class StrictScorecardTemplate(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var visible: Boolean,
    var distanceKm: Double,
    var status: StrictScorecardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictScorecardTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictScorecardTemplateStatus): Boolean {
        when (status) {
            StrictScorecardTemplateStatus.NEW -> {
                when (next) {
                    StrictScorecardTemplateStatus.QUEUED, StrictScorecardTemplateStatus.FAILED -> return true
                }
                return false
            }
            StrictScorecardTemplateStatus.QUEUED -> {
                when (next) {
                    StrictScorecardTemplateStatus.DONE -> return true
                }
                return false
            }
            StrictScorecardTemplateStatus.RUNNING -> {
                when (next) {
                    StrictScorecardTemplateStatus.FAILED -> return true
                }
                return false
            }
            StrictScorecardTemplateStatus.DONE -> {
                when (next) {
                    StrictScorecardTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictScorecardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("strict scorecard template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict scorecard template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((startsAt.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): StrictScorecardTemplate {
        return StrictScorecardTemplate(
            code = code,
            title = title,
            attributes = attributes,
            startsAt = startsAt,
            visible = visible,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict scorecard template in status new. */
fun newStrictScorecardTemplate(code: String, title: String): StrictScorecardTemplate {
    return StrictScorecardTemplate(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        visible = false,
        distanceKm = 0.0,
        status = StrictScorecardTemplateStatus.NEW,
    )
}
