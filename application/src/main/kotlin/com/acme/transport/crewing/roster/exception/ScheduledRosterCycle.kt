package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled roster cycle is in its life cycle. */
enum class ScheduledRosterCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled roster cycle and its current status. */
data class ScheduledRosterCycle(
    var code: String,
    var title: String,
    var stopCount: Int,
    var lane: String,
    var source: String,
    var attempts: Int,
    var sequence: Int,
    var status: ScheduledRosterCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterCycleStatus): Boolean {
        when (status) {
            ScheduledRosterCycleStatus.CREATED -> return next == ScheduledRosterCycleStatus.VALIDATED || next == ScheduledRosterCycleStatus.RETIRED
            ScheduledRosterCycleStatus.VALIDATED -> {
                when (next) {
                    ScheduledRosterCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledRosterCycleStatus.PUBLISHED -> {
                when (next) {
                    ScheduledRosterCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRosterCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((stopCount.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRosterCycle {
        return ScheduledRosterCycle(
            code = code,
            title = title,
            stopCount = stopCount,
            lane = lane,
            source = source,
            attempts = attempts,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled roster cycle in status created. */
fun newScheduledRosterCycle(code: String, title: String): ScheduledRosterCycle {
    return ScheduledRosterCycle(
        code = code,
        title = title,
        stopCount = 0,
        lane = "",
        source = "",
        attempts = 0,
        sequence = 0,
        status = ScheduledRosterCycleStatus.CREATED,
    )
}
