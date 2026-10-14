package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer yard schedule is in its life cycle. */
enum class ManualTrailerYardScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a manual trailer yard schedule and its current status. */
data class ManualTrailerYardSchedule(
    var code: String,
    var label: String,
    var weight: Double,
    var rate: Double,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var retries: Int,
    var status: ManualTrailerYardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerYardScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerYardScheduleStatus): Boolean {
        when (status) {
            ManualTrailerYardScheduleStatus.REQUESTED -> return next == ManualTrailerYardScheduleStatus.GRANTED || next == ManualTrailerYardScheduleStatus.REVOKED
            ManualTrailerYardScheduleStatus.GRANTED -> {
                when (next) {
                    ManualTrailerYardScheduleStatus.REVOKED, ManualTrailerYardScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            ManualTrailerYardScheduleStatus.REVOKED -> {
                when (next) {
                    ManualTrailerYardScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerYardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer yard schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual trailer yard schedule"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((weight - rate) * 100) / 100

    fun snapshot(): ManualTrailerYardSchedule {
        return ManualTrailerYardSchedule(
            code = code,
            label = label,
            weight = weight,
            rate = rate,
            approved = approved,
            attributes = attributes,
            archived = archived,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual trailer yard schedule in status requested. */
fun newManualTrailerYardSchedule(code: String, label: String): ManualTrailerYardSchedule {
    return ManualTrailerYardSchedule(
        code = code,
        label = label,
        weight = 0.0,
        rate = 0.0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        archived = false,
        retries = 0,
        status = ManualTrailerYardScheduleStatus.REQUESTED,
    )
}
