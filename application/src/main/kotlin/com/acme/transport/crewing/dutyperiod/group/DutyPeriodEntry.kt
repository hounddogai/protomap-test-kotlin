package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period entry is in its life cycle. */
enum class DutyPeriodEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a duty period entry and its current status. */
data class DutyPeriodEntry(
    var code: String,
    var label: String,
    var summary: String,
    var visible: Boolean,
    var revision: Int,
    var floorValue: Double,
    var status: DutyPeriodEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodEntryStatus): Boolean {
        when (status) {
            DutyPeriodEntryStatus.DRAFT -> {
                when (next) {
                    DutyPeriodEntryStatus.CLOSED -> return true
                }
                return false
            }
            DutyPeriodEntryStatus.PENDING -> return next == DutyPeriodEntryStatus.ACTIVE
            DutyPeriodEntryStatus.ACTIVE -> {
                when (next) {
                    DutyPeriodEntryStatus.SUSPENDED, DutyPeriodEntryStatus.CLOSED -> return true
                }
                return false
            }
            DutyPeriodEntryStatus.SUSPENDED -> {
                when (next) {
                    DutyPeriodEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodEntryStatus) {
        if (!canMoveTo(next)) {
            error("duty period entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round(floorValue * revision.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodEntry {
        return DutyPeriodEntry(
            code = code,
            label = label,
            summary = summary,
            visible = visible,
            revision = revision,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period entry in status draft. */
fun newDutyPeriodEntry(code: String, label: String): DutyPeriodEntry {
    return DutyPeriodEntry(
        code = code,
        label = label,
        summary = "",
        visible = false,
        revision = 0,
        floorValue = 0.0,
        status = DutyPeriodEntryStatus.DRAFT,
    )
}
