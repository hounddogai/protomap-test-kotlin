package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled roster profile is in its life cycle. */
enum class ScheduledRosterProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled roster profile as the roster team keeps it. */
data class ScheduledRosterProfile(
    var code: String,
    var label: String,
    var shiftCode: String,
    var phone: String,
    var driverId: String,
    var expiresAt: Int,
    var description: String,
    var endsAt: Int,
    var reviewer: String,
    var archived: Boolean,
    var status: ScheduledRosterProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterProfileStatus): Boolean {
        when (status) {
            ScheduledRosterProfileStatus.NEW -> {
                when (next) {
                    ScheduledRosterProfileStatus.DONE -> return true
                }
                return false
            }
            ScheduledRosterProfileStatus.QUEUED -> {
                when (next) {
                    ScheduledRosterProfileStatus.FAILED -> return true
                }
                return false
            }
            ScheduledRosterProfileStatus.RUNNING -> return next == ScheduledRosterProfileStatus.DONE || next == ScheduledRosterProfileStatus.FAILED
            ScheduledRosterProfileStatus.DONE -> return next == ScheduledRosterProfileStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledRosterProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster profile $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled roster profile"} $code"

    fun effectiveLoad(): Double = round((endsAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRosterProfile {
        return ScheduledRosterProfile(
            code = code,
            label = label,
            shiftCode = shiftCode,
            phone = phone,
            driverId = driverId,
            expiresAt = expiresAt,
            description = description,
            endsAt = endsAt,
            reviewer = reviewer,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled roster profile in status new. */
fun newScheduledRosterProfile(code: String, label: String): ScheduledRosterProfile {
    return ScheduledRosterProfile(
        code = code,
        label = label,
        shiftCode = "",
        phone = "",
        driverId = "",
        expiresAt = 0,
        description = "",
        endsAt = 0,
        reviewer = "",
        archived = false,
        status = ScheduledRosterProfileStatus.NEW,
    )
}
