package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt schedule is in its life cycle. */
enum class AttemptScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt schedule. Its code is unique within a tenant. */
data class AttemptSchedule(
    var code: String,
    var name: String,
    var approved: Boolean,
    var summary: String,
    var revision: Int,
    var batchSize: Int,
    var rate: Double,
    var status: AttemptScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptScheduleStatus): Boolean {
        when (status) {
            AttemptScheduleStatus.NEW -> {
                when (next) {
                    AttemptScheduleStatus.QUEUED, AttemptScheduleStatus.FAILED -> return true
                }
                return false
            }
            AttemptScheduleStatus.QUEUED -> {
                when (next) {
                    AttemptScheduleStatus.RUNNING, AttemptScheduleStatus.DONE -> return true
                }
                return false
            }
            AttemptScheduleStatus.RUNNING -> return next == AttemptScheduleStatus.DONE || next == AttemptScheduleStatus.FAILED
            AttemptScheduleStatus.DONE -> return next == AttemptScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AttemptScheduleStatus) {
        if (!canMoveTo(next)) {
            error("attempt schedule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round(revision.toDouble() * rate * 100) / 100

    fun snapshot(): AttemptSchedule {
        return AttemptSchedule(
            code = code,
            name = name,
            approved = approved,
            summary = summary,
            revision = revision,
            batchSize = batchSize,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a attempt schedule in status new. */
fun newAttemptSchedule(code: String, name: String): AttemptSchedule {
    return AttemptSchedule(
        code = code,
        name = name,
        approved = false,
        summary = "",
        revision = 0,
        batchSize = 0,
        rate = 0.0,
        status = AttemptScheduleStatus.NEW,
    )
}
