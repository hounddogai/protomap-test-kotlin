package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell draft is in its life cycle. */
enum class DwellDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dwell draft and its current status. */
data class DwellDraft(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var cost: Double,
    var unit: String,
    var segment: String,
    var channel: String,
    var priority: Int,
    var carrier: String,
    var status: DwellDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellDraftStatus): Boolean {
        when (status) {
            DwellDraftStatus.DRAFT -> return next == DwellDraftStatus.PENDING || next == DwellDraftStatus.CLOSED
            DwellDraftStatus.PENDING -> return next == DwellDraftStatus.SUSPENDED
            DwellDraftStatus.ACTIVE -> return next == DwellDraftStatus.SUSPENDED
            DwellDraftStatus.SUSPENDED -> {
                when (next) {
                    DwellDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellDraftStatus) {
        if (!canMoveTo(next)) {
            error("dwell draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round(priority.toDouble() * cost * 100) / 100

    fun snapshot(): DwellDraft {
        return DwellDraft(
            code = code,
            label = label,
            updatedAt = updatedAt,
            cost = cost,
            unit = unit,
            segment = segment,
            channel = channel,
            priority = priority,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a dwell draft in status draft. */
fun newDwellDraft(code: String, label: String): DwellDraft {
    return DwellDraft(
        code = code,
        label = label,
        updatedAt = 0,
        cost = 0.0,
        unit = "",
        segment = "",
        channel = "",
        priority = 0,
        carrier = "",
        status = DwellDraftStatus.DRAFT,
    )
}
