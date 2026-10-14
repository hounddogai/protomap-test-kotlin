package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop agreement is in its life cycle. */
enum class DropAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One drop agreement, with what the planners need to act on it. */
data class DropAgreement(
    var code: String,
    var label: String,
    var description: String,
    var ownerId: String,
    var stopCount: Int,
    var category: String,
    var attempts: Int,
    var maximum: Double,
    var minimum: Double,
    var ratio: Double,
    var durationMinutes: Int,
    var status: DropAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropAgreementStatus): Boolean {
        when (status) {
            DropAgreementStatus.OPEN -> {
                when (next) {
                    DropAgreementStatus.IN_REVIEW -> return true
                }
                return false
            }
            DropAgreementStatus.IN_REVIEW -> return next == DropAgreementStatus.RESOLVED || next == DropAgreementStatus.ARCHIVED
            DropAgreementStatus.RESOLVED -> return next == DropAgreementStatus.REOPENED || next == DropAgreementStatus.ARCHIVED
            DropAgreementStatus.REOPENED -> return next == DropAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropAgreementStatus) {
        if (!canMoveTo(next)) {
            error("drop agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop agreement"} $code"

    fun adjustedLoad(): Double = round((minimum + attempts.toDouble()) * 100) / 100

    fun snapshot(): DropAgreement {
        return DropAgreement(
            code = code,
            label = label,
            description = description,
            ownerId = ownerId,
            stopCount = stopCount,
            category = category,
            attempts = attempts,
            maximum = maximum,
            minimum = minimum,
            ratio = ratio,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a drop agreement in status open. */
fun newDropAgreement(code: String, label: String): DropAgreement {
    return DropAgreement(
        code = code,
        label = label,
        description = "",
        ownerId = "",
        stopCount = 0,
        category = "",
        attempts = 0,
        maximum = 0.0,
        minimum = 0.0,
        ratio = 0.0,
        durationMinutes = 0,
        status = DropAgreementStatus.OPEN,
    )
}
