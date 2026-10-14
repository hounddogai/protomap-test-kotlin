package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual duty period estimate is in its life cycle. */
enum class ManualDutyPeriodEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual duty period estimate as the duty period team keeps it. */
data class ManualDutyPeriodEstimate(
    var code: String,
    var name: String,
    var shiftCode: String,
    var pallets: Int,
    var depot: String,
    var trailerId: String,
    var driverId: String,
    var capacity: Int,
    var status: ManualDutyPeriodEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodEstimateStatus): Boolean {
        when (status) {
            ManualDutyPeriodEstimateStatus.CREATED -> return next == ManualDutyPeriodEstimateStatus.PUBLISHED || next == ManualDutyPeriodEstimateStatus.RETIRED
            ManualDutyPeriodEstimateStatus.VALIDATED -> return next == ManualDutyPeriodEstimateStatus.RETIRED
            ManualDutyPeriodEstimateStatus.PUBLISHED -> return next == ManualDutyPeriodEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual duty period estimate"} $code"

    fun weightedVolume(): Double = round(pallets.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ManualDutyPeriodEstimate {
        return ManualDutyPeriodEstimate(
            code = code,
            name = name,
            shiftCode = shiftCode,
            pallets = pallets,
            depot = depot,
            trailerId = trailerId,
            driverId = driverId,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual duty period estimate in status created. */
fun newManualDutyPeriodEstimate(code: String, name: String): ManualDutyPeriodEstimate {
    return ManualDutyPeriodEstimate(
        code = code,
        name = name,
        shiftCode = "",
        pallets = 0,
        depot = "",
        trailerId = "",
        driverId = "",
        capacity = 0,
        status = ManualDutyPeriodEstimateStatus.CREATED,
    )
}
