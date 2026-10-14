package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal qualification milestone is in its life cycle. */
enum class SeasonalQualificationMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal qualification milestone, with what the planners need to act on it. */
data class SeasonalQualificationMilestone(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var locale: String,
    var amount: Double,
    var externalRef: String,
    var durationMinutes: Int,
    var batchSize: Int,
    var approved: Boolean,
    var title: String,
    var region: String,
    var status: SeasonalQualificationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalQualificationMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalQualificationMilestoneStatus): Boolean {
        when (status) {
            SeasonalQualificationMilestoneStatus.PLANNED -> {
                when (next) {
                    SeasonalQualificationMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalQualificationMilestoneStatus.SCHEDULED -> {
                when (next) {
                    SeasonalQualificationMilestoneStatus.IN_PROGRESS, SeasonalQualificationMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalQualificationMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalQualificationMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalQualificationMilestoneStatus.COMPLETED -> return next == SeasonalQualificationMilestoneStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalQualificationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal qualification milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal qualification milestone"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((durationMinutes.toDouble() + amount) * 100) / 100

    fun snapshot(): SeasonalQualificationMilestone {
        return SeasonalQualificationMilestone(
            code = code,
            label = label,
            attributes = attributes,
            locale = locale,
            amount = amount,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            batchSize = batchSize,
            approved = approved,
            title = title,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal qualification milestone in status planned. */
fun newSeasonalQualificationMilestone(code: String, label: String): SeasonalQualificationMilestone {
    return SeasonalQualificationMilestone(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        locale = "",
        amount = 0.0,
        externalRef = "",
        durationMinutes = 0,
        batchSize = 0,
        approved = false,
        title = "",
        region = "",
        status = SeasonalQualificationMilestoneStatus.PLANNED,
    )
}
