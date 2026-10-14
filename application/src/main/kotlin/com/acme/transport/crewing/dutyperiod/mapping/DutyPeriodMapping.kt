package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period mapping records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period mapping is in its life cycle. */
enum class DutyPeriodMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One duty period mapping, with what the planners need to act on it. */
data class DutyPeriodMapping(
    var code: String,
    var name: String,
    var version: Int,
    var etaMinutes: Int,
    var limit: Int,
    var endsAt: Int,
    var budget: Double,
    var floorValue: Double,
    var retries: Int,
    var status: DutyPeriodMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodMappingStatus): Boolean {
        when (status) {
            DutyPeriodMappingStatus.NEW -> return next == DutyPeriodMappingStatus.DONE
            DutyPeriodMappingStatus.QUEUED -> return next == DutyPeriodMappingStatus.DONE
            DutyPeriodMappingStatus.RUNNING -> {
                when (next) {
                    DutyPeriodMappingStatus.DONE -> return true
                }
                return false
            }
            DutyPeriodMappingStatus.DONE -> {
                when (next) {
                    DutyPeriodMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodMappingStatus) {
        if (!canMoveTo(next)) {
            error("duty period mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round((version.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodMapping {
        return DutyPeriodMapping(
            code = code,
            name = name,
            version = version,
            etaMinutes = etaMinutes,
            limit = limit,
            endsAt = endsAt,
            budget = budget,
            floorValue = floorValue,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a duty period mapping in status new. */
fun newDutyPeriodMapping(code: String, name: String): DutyPeriodMapping {
    return DutyPeriodMapping(
        code = code,
        name = name,
        version = 0,
        etaMinutes = 0,
        limit = 0,
        endsAt = 0,
        budget = 0.0,
        floorValue = 0.0,
        retries = 0,
        status = DutyPeriodMappingStatus.NEW,
    )
}
