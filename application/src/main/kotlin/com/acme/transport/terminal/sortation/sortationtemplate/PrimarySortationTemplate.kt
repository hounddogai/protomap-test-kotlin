package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary sortation template records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a primary sortation template is in its life cycle. */
enum class PrimarySortationTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One primary sortation template, with what the planners need to act on it. */
data class PrimarySortationTemplate(
    var code: String,
    var title: String,
    var lane: String,
    var timezone: String,
    var stopCount: Int,
    var margin: Double,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var distanceKm: Double,
    var status: PrimarySortationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySortationTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySortationTemplateStatus): Boolean {
        when (status) {
            PrimarySortationTemplateStatus.DRAFT -> {
                when (next) {
                    PrimarySortationTemplateStatus.PENDING, PrimarySortationTemplateStatus.ACTIVE -> return true
                }
                return false
            }
            PrimarySortationTemplateStatus.PENDING -> {
                when (next) {
                    PrimarySortationTemplateStatus.ACTIVE, PrimarySortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            PrimarySortationTemplateStatus.ACTIVE -> {
                when (next) {
                    PrimarySortationTemplateStatus.SUSPENDED, PrimarySortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            PrimarySortationTemplateStatus.SUSPENDED -> {
                when (next) {
                    PrimarySortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimarySortationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("primary sortation template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary sortation template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((margin + distanceKm) * 100) / 100

    fun snapshot(): PrimarySortationTemplate {
        return PrimarySortationTemplate(
            code = code,
            title = title,
            lane = lane,
            timezone = timezone,
            stopCount = stopCount,
            margin = margin,
            trailerId = trailerId,
            attributes = attributes,
            endsAt = endsAt,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary sortation template in status draft. */
fun newPrimarySortationTemplate(code: String, title: String): PrimarySortationTemplate {
    return PrimarySortationTemplate(
        code = code,
        title = title,
        lane = "",
        timezone = "",
        stopCount = 0,
        margin = 0.0,
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        distanceKm = 0.0,
        status = PrimarySortationTemplateStatus.DRAFT,
    )
}
