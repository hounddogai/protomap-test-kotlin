package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period request records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period request is in its life cycle. */
enum class DutyPeriodRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A duty period request. Its code is unique within a tenant. */
data class DutyPeriodRequest(
    var code: String,
    var name: String,
    var currency: String,
    var loadKg: Double,
    var shiftCode: String,
    var carrier: String,
    var region: String,
    var channel: String,
    var graceDays: Int,
    var status: DutyPeriodRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodRequestStatus): Boolean {
        when (status) {
            DutyPeriodRequestStatus.PLANNED -> return next == DutyPeriodRequestStatus.SCHEDULED || next == DutyPeriodRequestStatus.IN_PROGRESS
            DutyPeriodRequestStatus.SCHEDULED -> {
                when (next) {
                    DutyPeriodRequestStatus.IN_PROGRESS, DutyPeriodRequestStatus.COMPLETED -> return true
                }
                return false
            }
            DutyPeriodRequestStatus.IN_PROGRESS -> return next == DutyPeriodRequestStatus.CANCELLED
            DutyPeriodRequestStatus.COMPLETED -> {
                when (next) {
                    DutyPeriodRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodRequestStatus) {
        if (!canMoveTo(next)) {
            error("duty period request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodRequest {
        return DutyPeriodRequest(
            code = code,
            name = name,
            currency = currency,
            loadKg = loadKg,
            shiftCode = shiftCode,
            carrier = carrier,
            region = region,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a duty period request in status planned. */
fun newDutyPeriodRequest(code: String, name: String): DutyPeriodRequest {
    return DutyPeriodRequest(
        code = code,
        name = name,
        currency = "",
        loadKg = 0.0,
        shiftCode = "",
        carrier = "",
        region = "",
        channel = "",
        graceDays = 0,
        status = DutyPeriodRequestStatus.PLANNED,
    )
}
