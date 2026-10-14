package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation preference records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a sortation preference is in its life cycle. */
enum class SortationPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a sortation preference and its current status. */
data class SortationPreference(
    var code: String,
    var name: String,
    var windowDays: Int,
    var limit: Int,
    var ratio: Double,
    var timezone: String,
    var expiresAt: Int,
    var channel: String,
    var priority: Int,
    var notes: String,
    var durationMinutes: Int,
    var status: SortationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationPreferenceStatus): Boolean {
        when (status) {
            SortationPreferenceStatus.REQUESTED -> {
                when (next) {
                    SortationPreferenceStatus.GRANTED -> return true
                }
                return false
            }
            SortationPreferenceStatus.GRANTED -> {
                when (next) {
                    SortationPreferenceStatus.REVOKED, SortationPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            SortationPreferenceStatus.REVOKED -> return next == SortationPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SortationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("sortation preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((priority.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): SortationPreference {
        return SortationPreference(
            code = code,
            name = name,
            windowDays = windowDays,
            limit = limit,
            ratio = ratio,
            timezone = timezone,
            expiresAt = expiresAt,
            channel = channel,
            priority = priority,
            notes = notes,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a sortation preference in status requested. */
fun newSortationPreference(code: String, name: String): SortationPreference {
    return SortationPreference(
        code = code,
        name = name,
        windowDays = 0,
        limit = 0,
        ratio = 0.0,
        timezone = "",
        expiresAt = 0,
        channel = "",
        priority = 0,
        notes = "",
        durationMinutes = 0,
        status = SortationPreferenceStatus.REQUESTED,
    )
}
