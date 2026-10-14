package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual violation window is in its life cycle. */
enum class ManualViolationWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual violation window. Its code is unique within a tenant. */
data class ManualViolationWindow(
    var code: String,
    var title: String,
    var priority: Int,
    var shiftCode: String,
    var description: String,
    var retries: Int,
    var durationMinutes: Int,
    var startsAt: Int,
    var limit: Int,
    var name: String,
    var graceDays: Int,
    var status: ManualViolationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualViolationWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualViolationWindowStatus): Boolean {
        when (status) {
            ManualViolationWindowStatus.DRAFT -> {
                when (next) {
                    ManualViolationWindowStatus.SUSPENDED, ManualViolationWindowStatus.CLOSED -> return true
                }
                return false
            }
            ManualViolationWindowStatus.PENDING -> return next == ManualViolationWindowStatus.ACTIVE || next == ManualViolationWindowStatus.SUSPENDED
            ManualViolationWindowStatus.ACTIVE -> {
                when (next) {
                    ManualViolationWindowStatus.SUSPENDED, ManualViolationWindowStatus.CLOSED -> return true
                }
                return false
            }
            ManualViolationWindowStatus.SUSPENDED -> {
                when (next) {
                    ManualViolationWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualViolationWindowStatus) {
        if (!canMoveTo(next)) {
            error("manual violation window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual violation window"} $code"

    fun adjustedExposure(): Double = round((graceDays.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): ManualViolationWindow {
        return ManualViolationWindow(
            code = code,
            title = title,
            priority = priority,
            shiftCode = shiftCode,
            description = description,
            retries = retries,
            durationMinutes = durationMinutes,
            startsAt = startsAt,
            limit = limit,
            name = name,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual violation window in status draft. */
fun newManualViolationWindow(code: String, title: String): ManualViolationWindow {
    return ManualViolationWindow(
        code = code,
        title = title,
        priority = 0,
        shiftCode = "",
        description = "",
        retries = 0,
        durationMinutes = 0,
        startsAt = 0,
        limit = 0,
        name = "",
        graceDays = 0,
        status = ManualViolationWindowStatus.DRAFT,
    )
}
