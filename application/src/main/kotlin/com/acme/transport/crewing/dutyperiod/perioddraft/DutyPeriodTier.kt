package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize

/** Where a duty period tier is in its life cycle. */
enum class DutyPeriodTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A duty period tier. Its code is unique within a tenant. */
data class DutyPeriodTier(
    var code: String,
    var label: String,
    var approved: Boolean,
    var channel: String,
    var etaMinutes: Int,
    var status: DutyPeriodTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodTierStatus): Boolean {
        when (status) {
            DutyPeriodTierStatus.OPEN -> return next == DutyPeriodTierStatus.REOPENED
            DutyPeriodTierStatus.IN_REVIEW -> return next == DutyPeriodTierStatus.REOPENED || next == DutyPeriodTierStatus.ARCHIVED
            DutyPeriodTierStatus.RESOLVED -> return next == DutyPeriodTierStatus.REOPENED || next == DutyPeriodTierStatus.ARCHIVED
            DutyPeriodTierStatus.REOPENED -> return next == DutyPeriodTierStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DutyPeriodTierStatus) {
        if (!canMoveTo(next)) {
            error("duty period tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): DutyPeriodTier {
        return DutyPeriodTier(
            code = code,
            label = label,
            approved = approved,
            channel = channel,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a duty period tier in status open. */
fun newDutyPeriodTier(code: String, label: String): DutyPeriodTier {
    return DutyPeriodTier(
        code = code,
        label = label,
        approved = false,
        channel = "",
        etaMinutes = 0,
        status = DutyPeriodTierStatus.OPEN,
    )
}
