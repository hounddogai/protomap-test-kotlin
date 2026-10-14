package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period checkpoint is in its life cycle. */
enum class DutyPeriodCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A duty period checkpoint. Its code is unique within a tenant. */
data class DutyPeriodCheckpoint(
    var code: String,
    var label: String,
    var ceiling: Double,
    var ratio: Double,
    var routeCode: String,
    var weight: Double,
    var priority: Int,
    var retries: Int,
    var country: String,
    var status: DutyPeriodCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodCheckpointStatus): Boolean {
        when (status) {
            DutyPeriodCheckpointStatus.CREATED -> return next == DutyPeriodCheckpointStatus.VALIDATED
            DutyPeriodCheckpointStatus.VALIDATED -> return next == DutyPeriodCheckpointStatus.RETIRED
            DutyPeriodCheckpointStatus.PUBLISHED -> return next == DutyPeriodCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DutyPeriodCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("duty period checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"duty period checkpoint"} $code"

    fun adjustedTotal(): Double = round(ceiling * ratio * 100) / 100

    fun snapshot(): DutyPeriodCheckpoint {
        return DutyPeriodCheckpoint(
            code = code,
            label = label,
            ceiling = ceiling,
            ratio = ratio,
            routeCode = routeCode,
            weight = weight,
            priority = priority,
            retries = retries,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a duty period checkpoint in status created. */
fun newDutyPeriodCheckpoint(code: String, label: String): DutyPeriodCheckpoint {
    return DutyPeriodCheckpoint(
        code = code,
        label = label,
        ceiling = 0.0,
        ratio = 0.0,
        routeCode = "",
        weight = 0.0,
        priority = 0,
        retries = 0,
        country = "",
        status = DutyPeriodCheckpointStatus.CREATED,
    )
}
