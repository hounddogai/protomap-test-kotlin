package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period score is in its life cycle. */
enum class DutyPeriodScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A duty period score as the duty period team keeps it. */
data class DutyPeriodScore(
    var code: String,
    var name: String,
    var timezone: String,
    var tags: MutableList<String>,
    var rate: Double,
    var batchSize: Int,
    var windowDays: Int,
    var minimum: Double,
    var status: DutyPeriodScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodScoreStatus): Boolean {
        when (status) {
            DutyPeriodScoreStatus.CREATED -> {
                when (next) {
                    DutyPeriodScoreStatus.VALIDATED, DutyPeriodScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            DutyPeriodScoreStatus.VALIDATED -> return next == DutyPeriodScoreStatus.PUBLISHED
            DutyPeriodScoreStatus.PUBLISHED -> {
                when (next) {
                    DutyPeriodScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodScoreStatus) {
        if (!canMoveTo(next)) {
            error("duty period score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((windowDays.toDouble() + minimum) * 100) / 100

    fun snapshot(): DutyPeriodScore {
        return DutyPeriodScore(
            code = code,
            name = name,
            timezone = timezone,
            tags = tags,
            rate = rate,
            batchSize = batchSize,
            windowDays = windowDays,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a duty period score in status created. */
fun newDutyPeriodScore(code: String, name: String): DutyPeriodScore {
    return DutyPeriodScore(
        code = code,
        name = name,
        timezone = "",
        tags = mutableListOf<String>(),
        rate = 0.0,
        batchSize = 0,
        windowDays = 0,
        minimum = 0.0,
        status = DutyPeriodScoreStatus.CREATED,
    )
}
