package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.text.normalize

/** Where a strict hold bundle is in its life cycle. */
enum class StrictHoldBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict hold bundle and its current status. */
data class StrictHoldBundle(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var title: String,
    var archived: Boolean,
    var pinned: Boolean,
    var summary: String,
    var unit: String,
    var status: StrictHoldBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictHoldBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictHoldBundleStatus): Boolean {
        when (status) {
            StrictHoldBundleStatus.IDLE -> {
                when (next) {
                    StrictHoldBundleStatus.WARMING -> return true
                }
                return false
            }
            StrictHoldBundleStatus.WARMING -> return next == StrictHoldBundleStatus.READY || next == StrictHoldBundleStatus.STOPPED
            StrictHoldBundleStatus.READY -> return next == StrictHoldBundleStatus.DRAINING || next == StrictHoldBundleStatus.STOPPED
            StrictHoldBundleStatus.DRAINING -> {
                when (next) {
                    StrictHoldBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictHoldBundleStatus) {
        if (!canMoveTo(next)) {
            error("strict hold bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): StrictHoldBundle {
        return StrictHoldBundle(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            title = title,
            archived = archived,
            pinned = pinned,
            summary = summary,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict hold bundle in status idle. */
fun newStrictHoldBundle(code: String, name: String): StrictHoldBundle {
    return StrictHoldBundle(
        code = code,
        name = name,
        durationMinutes = 0,
        title = "",
        archived = false,
        pinned = false,
        summary = "",
        unit = "",
        status = StrictHoldBundleStatus.IDLE,
    )
}
