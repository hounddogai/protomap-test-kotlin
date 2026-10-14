package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted roster draft is in its life cycle. */
enum class WeightedRosterDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One weighted roster draft, with what the planners need to act on it. */
data class WeightedRosterDraft(
    var code: String,
    var label: String,
    var quantity: Int,
    var graceDays: Int,
    var timezone: String,
    var floorValue: Double,
    var priority: Int,
    var loadKg: Double,
    var windowDays: Int,
    var category: String,
    var odometer: Int,
    var status: WeightedRosterDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRosterDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRosterDraftStatus): Boolean {
        when (status) {
            WeightedRosterDraftStatus.DRAFT -> return next == WeightedRosterDraftStatus.ACTIVE || next == WeightedRosterDraftStatus.CLOSED
            WeightedRosterDraftStatus.PENDING -> {
                when (next) {
                    WeightedRosterDraftStatus.CLOSED -> return true
                }
                return false
            }
            WeightedRosterDraftStatus.ACTIVE -> return next == WeightedRosterDraftStatus.CLOSED
            WeightedRosterDraftStatus.SUSPENDED -> {
                when (next) {
                    WeightedRosterDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRosterDraftStatus) {
        if (!canMoveTo(next)) {
            error("weighted roster draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((priority.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): WeightedRosterDraft {
        return WeightedRosterDraft(
            code = code,
            label = label,
            quantity = quantity,
            graceDays = graceDays,
            timezone = timezone,
            floorValue = floorValue,
            priority = priority,
            loadKg = loadKg,
            windowDays = windowDays,
            category = category,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted roster draft in status draft. */
fun newWeightedRosterDraft(code: String, label: String): WeightedRosterDraft {
    return WeightedRosterDraft(
        code = code,
        label = label,
        quantity = 0,
        graceDays = 0,
        timezone = "",
        floorValue = 0.0,
        priority = 0,
        loadKg = 0.0,
        windowDays = 0,
        category = "",
        odometer = 0,
        status = WeightedRosterDraftStatus.DRAFT,
    )
}
