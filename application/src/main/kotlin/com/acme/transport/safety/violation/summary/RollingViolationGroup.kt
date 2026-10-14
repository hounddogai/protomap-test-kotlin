package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling violation group is in its life cycle. */
enum class RollingViolationGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling violation group as the violation team keeps it. */
data class RollingViolationGroup(
    var code: String,
    var label: String,
    var reviewer: String,
    var version: Int,
    var currency: String,
    var approved: Boolean,
    var amount: Double,
    var dock: String,
    var loadKg: Double,
    var timezone: String,
    var minimum: Double,
    var status: RollingViolationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationGroupStatus): Boolean {
        when (status) {
            RollingViolationGroupStatus.OPEN -> {
                when (next) {
                    RollingViolationGroupStatus.RESOLVED -> return true
                }
                return false
            }
            RollingViolationGroupStatus.IN_REVIEW -> {
                when (next) {
                    RollingViolationGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingViolationGroupStatus.RESOLVED -> return next == RollingViolationGroupStatus.ARCHIVED
            RollingViolationGroupStatus.REOPENED -> return next == RollingViolationGroupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingViolationGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation group $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round((minimum + version.toDouble()) * 100) / 100

    fun snapshot(): RollingViolationGroup {
        return RollingViolationGroup(
            code = code,
            label = label,
            reviewer = reviewer,
            version = version,
            currency = currency,
            approved = approved,
            amount = amount,
            dock = dock,
            loadKg = loadKg,
            timezone = timezone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling violation group in status open. */
fun newRollingViolationGroup(code: String, label: String): RollingViolationGroup {
    return RollingViolationGroup(
        code = code,
        label = label,
        reviewer = "",
        version = 0,
        currency = "",
        approved = false,
        amount = 0.0,
        dock = "",
        loadKg = 0.0,
        timezone = "",
        minimum = 0.0,
        status = RollingViolationGroupStatus.OPEN,
    )
}
