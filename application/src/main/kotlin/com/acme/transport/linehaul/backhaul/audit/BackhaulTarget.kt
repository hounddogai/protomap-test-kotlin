package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize

/** Where a backhaul target is in its life cycle. */
enum class BackhaulTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One backhaul target, with what the planners need to act on it. */
data class BackhaulTarget(
    var code: String,
    var label: String,
    var graceDays: Int,
    var currency: String,
    var phone: String,
    var status: BackhaulTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulTargetStatus): Boolean {
        when (status) {
            BackhaulTargetStatus.IDLE -> {
                when (next) {
                    BackhaulTargetStatus.READY, BackhaulTargetStatus.DRAINING -> return true
                }
                return false
            }
            BackhaulTargetStatus.WARMING -> {
                when (next) {
                    BackhaulTargetStatus.READY, BackhaulTargetStatus.DRAINING -> return true
                }
                return false
            }
            BackhaulTargetStatus.READY -> return next == BackhaulTargetStatus.DRAINING
            BackhaulTargetStatus.DRAINING -> return next == BackhaulTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BackhaulTargetStatus) {
        if (!canMoveTo(next)) {
            error("backhaul target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): BackhaulTarget {
        return BackhaulTarget(
            code = code,
            label = label,
            graceDays = graceDays,
            currency = currency,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul target in status idle. */
fun newBackhaulTarget(code: String, label: String): BackhaulTarget {
    return BackhaulTarget(
        code = code,
        label = label,
        graceDays = 0,
        currency = "",
        phone = "",
        status = BackhaulTargetStatus.IDLE,
    )
}
