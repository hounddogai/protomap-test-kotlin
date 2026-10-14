package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff schedule is in its life cycle. */
enum class CutoffScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff schedule. Its code is unique within a tenant. */
data class CutoffSchedule(
    var code: String,
    var title: String,
    var revision: Int,
    var capacity: Int,
    var startsAt: Int,
    var notes: String,
    var currency: String,
    var trailerId: String,
    var limit: Int,
    var status: CutoffScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffScheduleStatus): Boolean {
        when (status) {
            CutoffScheduleStatus.NEW -> {
                when (next) {
                    CutoffScheduleStatus.QUEUED, CutoffScheduleStatus.DONE -> return true
                }
                return false
            }
            CutoffScheduleStatus.QUEUED -> return next == CutoffScheduleStatus.RUNNING || next == CutoffScheduleStatus.FAILED
            CutoffScheduleStatus.RUNNING -> return next == CutoffScheduleStatus.DONE || next == CutoffScheduleStatus.FAILED
            CutoffScheduleStatus.DONE -> return next == CutoffScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffScheduleStatus) {
        if (!canMoveTo(next)) {
            error("cutoff schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round(revision.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): CutoffSchedule {
        return CutoffSchedule(
            code = code,
            title = title,
            revision = revision,
            capacity = capacity,
            startsAt = startsAt,
            notes = notes,
            currency = currency,
            trailerId = trailerId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff schedule in status new. */
fun newCutoffSchedule(code: String, title: String): CutoffSchedule {
    return CutoffSchedule(
        code = code,
        title = title,
        revision = 0,
        capacity = 0,
        startsAt = 0,
        notes = "",
        currency = "",
        trailerId = "",
        limit = 0,
        status = CutoffScheduleStatus.NEW,
    )
}
