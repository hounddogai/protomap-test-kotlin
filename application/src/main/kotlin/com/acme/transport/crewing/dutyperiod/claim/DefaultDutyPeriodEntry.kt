package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.normalize

/** Where a default duty period entry is in its life cycle. */
enum class DefaultDutyPeriodEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default duty period entry and its current status. */
data class DefaultDutyPeriodEntry(
    var code: String,
    var label: String,
    var region: String,
    var revision: Int,
    var externalRef: String,
    var status: DefaultDutyPeriodEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDutyPeriodEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDutyPeriodEntryStatus): Boolean {
        when (status) {
            DefaultDutyPeriodEntryStatus.REQUESTED -> return next == DefaultDutyPeriodEntryStatus.EXPIRED
            DefaultDutyPeriodEntryStatus.GRANTED -> {
                when (next) {
                    DefaultDutyPeriodEntryStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultDutyPeriodEntryStatus.REVOKED -> return next == DefaultDutyPeriodEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultDutyPeriodEntryStatus) {
        if (!canMoveTo(next)) {
            error("default duty period entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): DefaultDutyPeriodEntry {
        return DefaultDutyPeriodEntry(
            code = code,
            label = label,
            region = region,
            revision = revision,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default duty period entry in status requested. */
fun newDefaultDutyPeriodEntry(code: String, label: String): DefaultDutyPeriodEntry {
    return DefaultDutyPeriodEntry(
        code = code,
        label = label,
        region = "",
        revision = 0,
        externalRef = "",
        status = DefaultDutyPeriodEntryStatus.REQUESTED,
    )
}
