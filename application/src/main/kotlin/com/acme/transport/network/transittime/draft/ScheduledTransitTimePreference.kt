package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled transit time preference is in its life cycle. */
enum class ScheduledTransitTimePreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled transit time preference and its current status. */
data class ScheduledTransitTimePreference(
    var code: String,
    var title: String,
    var rate: Double,
    var windowDays: Int,
    var archived: Boolean,
    var limit: Int,
    var timezone: String,
    var durationMinutes: Int,
    var floorValue: Double,
    var status: ScheduledTransitTimePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTransitTimePreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTransitTimePreferenceStatus): Boolean {
        when (status) {
            ScheduledTransitTimePreferenceStatus.OPEN -> return next == ScheduledTransitTimePreferenceStatus.IN_REVIEW
            ScheduledTransitTimePreferenceStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledTransitTimePreferenceStatus.REOPENED, ScheduledTransitTimePreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledTransitTimePreferenceStatus.RESOLVED -> return next == ScheduledTransitTimePreferenceStatus.REOPENED
            ScheduledTransitTimePreferenceStatus.REOPENED -> {
                when (next) {
                    ScheduledTransitTimePreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTransitTimePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled transit time preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((floorValue + limit.toDouble()) * 100) / 100

    fun snapshot(): ScheduledTransitTimePreference {
        return ScheduledTransitTimePreference(
            code = code,
            title = title,
            rate = rate,
            windowDays = windowDays,
            archived = archived,
            limit = limit,
            timezone = timezone,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled transit time preference in status open. */
fun newScheduledTransitTimePreference(code: String, title: String): ScheduledTransitTimePreference {
    return ScheduledTransitTimePreference(
        code = code,
        title = title,
        rate = 0.0,
        windowDays = 0,
        archived = false,
        limit = 0,
        timezone = "",
        durationMinutes = 0,
        floorValue = 0.0,
        status = ScheduledTransitTimePreferenceStatus.OPEN,
    )
}
