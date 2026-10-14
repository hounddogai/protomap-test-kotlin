package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize

/** Where a global duty period estimate is in its life cycle. */
enum class GlobalDutyPeriodEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global duty period estimate, with what the planners need to act on it. */
data class GlobalDutyPeriodEstimate(
    var code: String,
    var title: String,
    var notes: String,
    var maximum: Double,
    var phone: String,
    var country: String,
    var unit: String,
    var status: GlobalDutyPeriodEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDutyPeriodEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDutyPeriodEstimateStatus): Boolean {
        when (status) {
            GlobalDutyPeriodEstimateStatus.PROPOSED -> {
                when (next) {
                    GlobalDutyPeriodEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalDutyPeriodEstimateStatus.APPROVED -> return next == GlobalDutyPeriodEstimateStatus.REJECTED || next == GlobalDutyPeriodEstimateStatus.WITHDRAWN
            GlobalDutyPeriodEstimateStatus.REJECTED -> return next == GlobalDutyPeriodEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalDutyPeriodEstimateStatus) {
        if (!canMoveTo(next)) {
            error("global duty period estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global duty period estimate"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): GlobalDutyPeriodEstimate {
        return GlobalDutyPeriodEstimate(
            code = code,
            title = title,
            notes = notes,
            maximum = maximum,
            phone = phone,
            country = country,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global duty period estimate in status proposed. */
fun newGlobalDutyPeriodEstimate(code: String, title: String): GlobalDutyPeriodEstimate {
    return GlobalDutyPeriodEstimate(
        code = code,
        title = title,
        notes = "",
        maximum = 0.0,
        phone = "",
        country = "",
        unit = "",
        status = GlobalDutyPeriodEstimateStatus.PROPOSED,
    )
}
