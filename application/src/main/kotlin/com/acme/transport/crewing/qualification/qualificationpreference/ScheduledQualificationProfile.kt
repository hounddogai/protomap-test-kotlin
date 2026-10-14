package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled qualification profile is in its life cycle. */
enum class ScheduledQualificationProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled qualification profile, with what the planners need to act on it. */
data class ScheduledQualificationProfile(
    var code: String,
    var title: String,
    var startsAt: Int,
    var quantity: Int,
    var expiresAt: Int,
    var retries: Int,
    var limit: Int,
    var budget: Double,
    var attempts: Int,
    var status: ScheduledQualificationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledQualificationProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledQualificationProfileStatus): Boolean {
        when (status) {
            ScheduledQualificationProfileStatus.NEW -> return next == ScheduledQualificationProfileStatus.RUNNING || next == ScheduledQualificationProfileStatus.FAILED
            ScheduledQualificationProfileStatus.QUEUED -> return next == ScheduledQualificationProfileStatus.RUNNING || next == ScheduledQualificationProfileStatus.FAILED
            ScheduledQualificationProfileStatus.RUNNING -> {
                when (next) {
                    ScheduledQualificationProfileStatus.DONE, ScheduledQualificationProfileStatus.FAILED -> return true
                }
                return false
            }
            ScheduledQualificationProfileStatus.DONE -> return next == ScheduledQualificationProfileStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledQualificationProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled qualification profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled qualification profile"} $code"

    fun projectedExposure(): Double = round((retries.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledQualificationProfile {
        return ScheduledQualificationProfile(
            code = code,
            title = title,
            startsAt = startsAt,
            quantity = quantity,
            expiresAt = expiresAt,
            retries = retries,
            limit = limit,
            budget = budget,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled qualification profile in status new. */
fun newScheduledQualificationProfile(code: String, title: String): ScheduledQualificationProfile {
    return ScheduledQualificationProfile(
        code = code,
        title = title,
        startsAt = 0,
        quantity = 0,
        expiresAt = 0,
        retries = 0,
        limit = 0,
        budget = 0.0,
        attempts = 0,
        status = ScheduledQualificationProfileStatus.NEW,
    )
}
