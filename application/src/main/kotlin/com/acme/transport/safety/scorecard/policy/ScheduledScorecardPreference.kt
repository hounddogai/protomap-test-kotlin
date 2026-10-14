package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled scorecard preference is in its life cycle. */
enum class ScheduledScorecardPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled scorecard preference, with what the planners need to act on it. */
data class ScheduledScorecardPreference(
    var code: String,
    var label: String,
    var ratio: Double,
    var timezone: String,
    var updatedAt: Int,
    var externalRef: String,
    var startsAt: Int,
    var status: ScheduledScorecardPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledScorecardPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledScorecardPreferenceStatus): Boolean {
        when (status) {
            ScheduledScorecardPreferenceStatus.NEW -> {
                when (next) {
                    ScheduledScorecardPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledScorecardPreferenceStatus.QUEUED -> return next == ScheduledScorecardPreferenceStatus.RUNNING || next == ScheduledScorecardPreferenceStatus.FAILED
            ScheduledScorecardPreferenceStatus.RUNNING -> {
                when (next) {
                    ScheduledScorecardPreferenceStatus.DONE, ScheduledScorecardPreferenceStatus.FAILED -> return true
                }
                return false
            }
            ScheduledScorecardPreferenceStatus.DONE -> return next == ScheduledScorecardPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledScorecardPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled scorecard preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round((ratio - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledScorecardPreference {
        return ScheduledScorecardPreference(
            code = code,
            label = label,
            ratio = ratio,
            timezone = timezone,
            updatedAt = updatedAt,
            externalRef = externalRef,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled scorecard preference in status new. */
fun newScheduledScorecardPreference(code: String, label: String): ScheduledScorecardPreference {
    return ScheduledScorecardPreference(
        code = code,
        label = label,
        ratio = 0.0,
        timezone = "",
        updatedAt = 0,
        externalRef = "",
        startsAt = 0,
        status = ScheduledScorecardPreferenceStatus.NEW,
    )
}
