package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual attempt baseline records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a manual attempt baseline is in its life cycle. */
enum class ManualAttemptBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual attempt baseline. Its code is unique within a tenant. */
data class ManualAttemptBaseline(
    var code: String,
    var title: String,
    var retries: Int,
    var timezone: String,
    var driverId: String,
    var sequence: Int,
    var quantity: Int,
    var label: String,
    var externalRef: String,
    var odometer: Int,
    var status: ManualAttemptBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAttemptBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAttemptBaselineStatus): Boolean {
        when (status) {
            ManualAttemptBaselineStatus.DRAFT -> return next == ManualAttemptBaselineStatus.PENDING || next == ManualAttemptBaselineStatus.SUSPENDED
            ManualAttemptBaselineStatus.PENDING -> {
                when (next) {
                    ManualAttemptBaselineStatus.ACTIVE -> return true
                }
                return false
            }
            ManualAttemptBaselineStatus.ACTIVE -> return next == ManualAttemptBaselineStatus.SUSPENDED || next == ManualAttemptBaselineStatus.CLOSED
            ManualAttemptBaselineStatus.SUSPENDED -> {
                when (next) {
                    ManualAttemptBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAttemptBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual attempt baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedLoad(): Double = round(retries.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): ManualAttemptBaseline {
        return ManualAttemptBaseline(
            code = code,
            title = title,
            retries = retries,
            timezone = timezone,
            driverId = driverId,
            sequence = sequence,
            quantity = quantity,
            label = label,
            externalRef = externalRef,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual attempt baseline in status draft. */
fun newManualAttemptBaseline(code: String, title: String): ManualAttemptBaseline {
    return ManualAttemptBaseline(
        code = code,
        title = title,
        retries = 0,
        timezone = "",
        driverId = "",
        sequence = 0,
        quantity = 0,
        label = "",
        externalRef = "",
        odometer = 0,
        status = ManualAttemptBaselineStatus.DRAFT,
    )
}
