package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation baseline is in its life cycle. */
enum class ViolationBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One violation baseline, with what the planners need to act on it. */
data class ViolationBaseline(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var name: String,
    var revision: Int,
    var budget: Double,
    var cost: Double,
    var channel: String,
    var timezone: String,
    var status: ViolationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationBaselineStatus): Boolean {
        when (status) {
            ViolationBaselineStatus.DRAFT -> return next == ViolationBaselineStatus.SUSPENDED || next == ViolationBaselineStatus.CLOSED
            ViolationBaselineStatus.PENDING -> return next == ViolationBaselineStatus.ACTIVE || next == ViolationBaselineStatus.CLOSED
            ViolationBaselineStatus.ACTIVE -> return next == ViolationBaselineStatus.SUSPENDED || next == ViolationBaselineStatus.CLOSED
            ViolationBaselineStatus.SUSPENDED -> {
                when (next) {
                    ViolationBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("violation baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round(budget * revision.toDouble() * 100) / 100

    fun snapshot(): ViolationBaseline {
        return ViolationBaseline(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            name = name,
            revision = revision,
            budget = budget,
            cost = cost,
            channel = channel,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a violation baseline in status draft. */
fun newViolationBaseline(code: String, label: String): ViolationBaseline {
    return ViolationBaseline(
        code = code,
        label = label,
        etaMinutes = 0,
        name = "",
        revision = 0,
        budget = 0.0,
        cost = 0.0,
        channel = "",
        timezone = "",
        status = ViolationBaselineStatus.DRAFT,
    )
}
