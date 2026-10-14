package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a capacity draft is in its life cycle. */
enum class CapacityDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capacity draft as the capacity team keeps it. */
data class CapacityDraft(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var unit: String,
    var channel: String,
    var amount: Double,
    var status: CapacityDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityDraftStatus): Boolean {
        when (status) {
            CapacityDraftStatus.NEW -> {
                when (next) {
                    CapacityDraftStatus.QUEUED, CapacityDraftStatus.RUNNING -> return true
                }
                return false
            }
            CapacityDraftStatus.QUEUED -> {
                when (next) {
                    CapacityDraftStatus.RUNNING, CapacityDraftStatus.DONE -> return true
                }
                return false
            }
            CapacityDraftStatus.RUNNING -> return next == CapacityDraftStatus.DONE || next == CapacityDraftStatus.FAILED
            CapacityDraftStatus.DONE -> return next == CapacityDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CapacityDraftStatus) {
        if (!canMoveTo(next)) {
            error("capacity draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): CapacityDraft {
        return CapacityDraft(
            code = code,
            title = title,
            pinned = pinned,
            unit = unit,
            channel = channel,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capacity draft in status new. */
fun newCapacityDraft(code: String, title: String): CapacityDraft {
    return CapacityDraft(
        code = code,
        title = title,
        pinned = false,
        unit = "",
        channel = "",
        amount = 0.0,
        status = CapacityDraftStatus.NEW,
    )
}
