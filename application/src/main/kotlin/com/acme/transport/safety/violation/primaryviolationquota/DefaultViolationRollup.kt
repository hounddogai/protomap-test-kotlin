package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default violation rollup is in its life cycle. */
enum class DefaultViolationRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default violation rollup, with what the planners need to act on it. */
data class DefaultViolationRollup(
    var code: String,
    var label: String,
    var amount: Double,
    var title: String,
    var sequence: Int,
    var contactEmail: String,
    var dock: String,
    var pinned: Boolean,
    var checksum: String,
    var timezone: String,
    var status: DefaultViolationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultViolationRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultViolationRollupStatus): Boolean {
        when (status) {
            DefaultViolationRollupStatus.OPEN -> {
                when (next) {
                    DefaultViolationRollupStatus.RESOLVED -> return true
                }
                return false
            }
            DefaultViolationRollupStatus.IN_REVIEW -> return next == DefaultViolationRollupStatus.REOPENED
            DefaultViolationRollupStatus.RESOLVED -> return next == DefaultViolationRollupStatus.REOPENED || next == DefaultViolationRollupStatus.ARCHIVED
            DefaultViolationRollupStatus.REOPENED -> {
                when (next) {
                    DefaultViolationRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultViolationRollupStatus) {
        if (!canMoveTo(next)) {
            error("default violation rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((amount + sequence.toDouble()) * 100) / 100

    fun snapshot(): DefaultViolationRollup {
        return DefaultViolationRollup(
            code = code,
            label = label,
            amount = amount,
            title = title,
            sequence = sequence,
            contactEmail = contactEmail,
            dock = dock,
            pinned = pinned,
            checksum = checksum,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default violation rollup in status open. */
fun newDefaultViolationRollup(code: String, label: String): DefaultViolationRollup {
    return DefaultViolationRollup(
        code = code,
        label = label,
        amount = 0.0,
        title = "",
        sequence = 0,
        contactEmail = "",
        dock = "",
        pinned = false,
        checksum = "",
        timezone = "",
        status = DefaultViolationRollupStatus.OPEN,
    )
}
