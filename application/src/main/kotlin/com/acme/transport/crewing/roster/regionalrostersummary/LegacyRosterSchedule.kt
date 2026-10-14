package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy roster schedule is in its life cycle. */
enum class LegacyRosterScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy roster schedule. Its code is unique within a tenant. */
data class LegacyRosterSchedule(
    var code: String,
    var name: String,
    var createdAt: Int,
    var archived: Boolean,
    var attempts: Int,
    var ratio: Double,
    var title: String,
    var pinned: Boolean,
    var carrier: String,
    var ownerId: String,
    var status: LegacyRosterScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRosterScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRosterScheduleStatus): Boolean {
        when (status) {
            LegacyRosterScheduleStatus.NEW -> return next == LegacyRosterScheduleStatus.RUNNING || next == LegacyRosterScheduleStatus.DONE
            LegacyRosterScheduleStatus.QUEUED -> {
                when (next) {
                    LegacyRosterScheduleStatus.RUNNING -> return true
                }
                return false
            }
            LegacyRosterScheduleStatus.RUNNING -> return next == LegacyRosterScheduleStatus.DONE || next == LegacyRosterScheduleStatus.FAILED
            LegacyRosterScheduleStatus.DONE -> {
                when (next) {
                    LegacyRosterScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRosterScheduleStatus) {
        if (!canMoveTo(next)) {
            error("legacy roster schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy roster schedule"} $code"

    fun effectiveValue(): Double = round((ratio + attempts.toDouble()) * 100) / 100

    fun snapshot(): LegacyRosterSchedule {
        return LegacyRosterSchedule(
            code = code,
            name = name,
            createdAt = createdAt,
            archived = archived,
            attempts = attempts,
            ratio = ratio,
            title = title,
            pinned = pinned,
            carrier = carrier,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy roster schedule in status new. */
fun newLegacyRosterSchedule(code: String, name: String): LegacyRosterSchedule {
    return LegacyRosterSchedule(
        code = code,
        name = name,
        createdAt = 0,
        archived = false,
        attempts = 0,
        ratio = 0.0,
        title = "",
        pinned = false,
        carrier = "",
        ownerId = "",
        status = LegacyRosterScheduleStatus.NEW,
    )
}
