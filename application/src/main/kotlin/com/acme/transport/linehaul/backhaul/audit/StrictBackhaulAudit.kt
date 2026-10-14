package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict backhaul audit is in its life cycle. */
enum class StrictBackhaulAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict backhaul audit, with what the planners need to act on it. */
data class StrictBackhaulAudit(
    var code: String,
    var title: String,
    var priority: Int,
    var durationMinutes: Int,
    var amount: Double,
    var score: Double,
    var maximum: Double,
    var name: String,
    var graceDays: Int,
    var approved: Boolean,
    var status: StrictBackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulAuditStatus): Boolean {
        when (status) {
            StrictBackhaulAuditStatus.NEW -> {
                when (next) {
                    StrictBackhaulAuditStatus.RUNNING, StrictBackhaulAuditStatus.DONE -> return true
                }
                return false
            }
            StrictBackhaulAuditStatus.QUEUED -> {
                when (next) {
                    StrictBackhaulAuditStatus.RUNNING -> return true
                }
                return false
            }
            StrictBackhaulAuditStatus.RUNNING -> {
                when (next) {
                    StrictBackhaulAuditStatus.DONE, StrictBackhaulAuditStatus.FAILED -> return true
                }
                return false
            }
            StrictBackhaulAuditStatus.DONE -> return next == StrictBackhaulAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictBackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((priority.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): StrictBackhaulAudit {
        return StrictBackhaulAudit(
            code = code,
            title = title,
            priority = priority,
            durationMinutes = durationMinutes,
            amount = amount,
            score = score,
            maximum = maximum,
            name = name,
            graceDays = graceDays,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict backhaul audit in status new. */
fun newStrictBackhaulAudit(code: String, title: String): StrictBackhaulAudit {
    return StrictBackhaulAudit(
        code = code,
        title = title,
        priority = 0,
        durationMinutes = 0,
        amount = 0.0,
        score = 0.0,
        maximum = 0.0,
        name = "",
        graceDays = 0,
        approved = false,
        status = StrictBackhaulAuditStatus.NEW,
    )
}
