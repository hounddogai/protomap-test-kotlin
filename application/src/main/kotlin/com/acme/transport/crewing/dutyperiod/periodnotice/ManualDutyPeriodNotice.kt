package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.normalize

// The manual duty period notice records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a manual duty period notice is in its life cycle. */
enum class ManualDutyPeriodNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual duty period notice as the duty period team keeps it. */
data class ManualDutyPeriodNotice(
    var code: String,
    var label: String,
    var channel: String,
    var unit: String,
    var depot: String,
    var locale: String,
    var budget: Double,
    var status: ManualDutyPeriodNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodNoticeStatus): Boolean {
        when (status) {
            ManualDutyPeriodNoticeStatus.PROPOSED -> return next == ManualDutyPeriodNoticeStatus.APPROVED || next == ManualDutyPeriodNoticeStatus.WITHDRAWN
            ManualDutyPeriodNoticeStatus.APPROVED -> return next == ManualDutyPeriodNoticeStatus.REJECTED || next == ManualDutyPeriodNoticeStatus.WITHDRAWN
            ManualDutyPeriodNoticeStatus.REJECTED -> {
                when (next) {
                    ManualDutyPeriodNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodNoticeStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = budget

    fun snapshot(): ManualDutyPeriodNotice {
        return ManualDutyPeriodNotice(
            code = code,
            label = label,
            channel = channel,
            unit = unit,
            depot = depot,
            locale = locale,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual duty period notice in status proposed. */
fun newManualDutyPeriodNotice(code: String, label: String): ManualDutyPeriodNotice {
    return ManualDutyPeriodNotice(
        code = code,
        label = label,
        channel = "",
        unit = "",
        depot = "",
        locale = "",
        budget = 0.0,
        status = ManualDutyPeriodNoticeStatus.PROPOSED,
    )
}
