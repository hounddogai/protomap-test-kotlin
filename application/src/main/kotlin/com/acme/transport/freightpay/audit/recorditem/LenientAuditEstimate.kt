package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient audit estimate is in its life cycle. */
enum class LenientAuditEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient audit estimate, with what the planners need to act on it. */
data class LenientAuditEstimate(
    var code: String,
    var label: String,
    var quantity: Int,
    var priority: Int,
    var phone: String,
    var maximum: Double,
    var timezone: String,
    var pinned: Boolean,
    var minimum: Double,
    var status: LenientAuditEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAuditEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAuditEstimateStatus): Boolean {
        when (status) {
            LenientAuditEstimateStatus.DRAFT -> return next == LenientAuditEstimateStatus.SUSPENDED
            LenientAuditEstimateStatus.PENDING -> {
                when (next) {
                    LenientAuditEstimateStatus.ACTIVE, LenientAuditEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientAuditEstimateStatus.ACTIVE -> {
                when (next) {
                    LenientAuditEstimateStatus.CLOSED -> return true
                }
                return false
            }
            LenientAuditEstimateStatus.SUSPENDED -> return next == LenientAuditEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientAuditEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient audit estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((maximum - priority.toDouble()) * 100) / 100

    fun snapshot(): LenientAuditEstimate {
        return LenientAuditEstimate(
            code = code,
            label = label,
            quantity = quantity,
            priority = priority,
            phone = phone,
            maximum = maximum,
            timezone = timezone,
            pinned = pinned,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient audit estimate in status draft. */
fun newLenientAuditEstimate(code: String, label: String): LenientAuditEstimate {
    return LenientAuditEstimate(
        code = code,
        label = label,
        quantity = 0,
        priority = 0,
        phone = "",
        maximum = 0.0,
        timezone = "",
        pinned = false,
        minimum = 0.0,
        status = LenientAuditEstimateStatus.DRAFT,
    )
}
