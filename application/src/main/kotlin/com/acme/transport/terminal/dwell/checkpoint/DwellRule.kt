package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell rule is in its life cycle. */
enum class DwellRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell rule, with what the planners need to act on it. */
data class DwellRule(
    var code: String,
    var name: String,
    var stopCount: Int,
    var summary: String,
    var retries: Int,
    var dock: String,
    var channel: String,
    var odometer: Int,
    var depot: String,
    var endsAt: Int,
    var margin: Double,
    var status: DwellRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellRuleStatus): Boolean {
        when (status) {
            DwellRuleStatus.DRAFT -> return next == DwellRuleStatus.SUSPENDED
            DwellRuleStatus.PENDING -> {
                when (next) {
                    DwellRuleStatus.ACTIVE, DwellRuleStatus.CLOSED -> return true
                }
                return false
            }
            DwellRuleStatus.ACTIVE -> return next == DwellRuleStatus.SUSPENDED
            DwellRuleStatus.SUSPENDED -> return next == DwellRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DwellRuleStatus) {
        if (!canMoveTo(next)) {
            error("dwell rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(retries.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): DwellRule {
        return DwellRule(
            code = code,
            name = name,
            stopCount = stopCount,
            summary = summary,
            retries = retries,
            dock = dock,
            channel = channel,
            odometer = odometer,
            depot = depot,
            endsAt = endsAt,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dwell rule in status draft. */
fun newDwellRule(code: String, name: String): DwellRule {
    return DwellRule(
        code = code,
        name = name,
        stopCount = 0,
        summary = "",
        retries = 0,
        dock = "",
        channel = "",
        odometer = 0,
        depot = "",
        endsAt = 0,
        margin = 0.0,
        status = DwellRuleStatus.DRAFT,
    )
}
