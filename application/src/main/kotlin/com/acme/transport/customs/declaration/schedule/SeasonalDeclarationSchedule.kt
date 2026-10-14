package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal declaration schedule records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a seasonal declaration schedule is in its life cycle. */
enum class SeasonalDeclarationScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal declaration schedule. Its code is unique within a tenant. */
data class SeasonalDeclarationSchedule(
    var code: String,
    var label: String,
    var category: String,
    var cost: Double,
    var source: String,
    var title: String,
    var unit: String,
    var maximum: Double,
    var timezone: String,
    var contactEmail: String,
    var sequence: Int,
    var status: SeasonalDeclarationScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationScheduleStatus): Boolean {
        when (status) {
            SeasonalDeclarationScheduleStatus.REQUESTED -> {
                when (next) {
                    SeasonalDeclarationScheduleStatus.GRANTED -> return true
                }
                return false
            }
            SeasonalDeclarationScheduleStatus.GRANTED -> {
                when (next) {
                    SeasonalDeclarationScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalDeclarationScheduleStatus.REVOKED -> {
                when (next) {
                    SeasonalDeclarationScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal declaration schedule"} $code"

    fun netExposure(): Double = round(maximum * sequence.toDouble() * 100) / 100

    fun snapshot(): SeasonalDeclarationSchedule {
        return SeasonalDeclarationSchedule(
            code = code,
            label = label,
            category = category,
            cost = cost,
            source = source,
            title = title,
            unit = unit,
            maximum = maximum,
            timezone = timezone,
            contactEmail = contactEmail,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal declaration schedule in status requested. */
fun newSeasonalDeclarationSchedule(code: String, label: String): SeasonalDeclarationSchedule {
    return SeasonalDeclarationSchedule(
        code = code,
        label = label,
        category = "",
        cost = 0.0,
        source = "",
        title = "",
        unit = "",
        maximum = 0.0,
        timezone = "",
        contactEmail = "",
        sequence = 0,
        status = SeasonalDeclarationScheduleStatus.REQUESTED,
    )
}
