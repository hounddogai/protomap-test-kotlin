package com.acme.transport.crewing.swap.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap audit records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap audit is in its life cycle. */
enum class SwapAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A swap audit as the swap team keeps it. */
data class SwapAudit(
    var code: String,
    var label: String,
    var version: Int,
    var quantity: Int,
    var externalRef: String,
    var etaMinutes: Int,
    var summary: String,
    var graceDays: Int,
    var maximum: Double,
    var pinned: Boolean,
    var timezone: String,
    var status: SwapAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapAuditStatus): Boolean {
        when (status) {
            SwapAuditStatus.PROPOSED -> {
                when (next) {
                    SwapAuditStatus.APPROVED, SwapAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            SwapAuditStatus.APPROVED -> return next == SwapAuditStatus.REJECTED
            SwapAuditStatus.REJECTED -> {
                when (next) {
                    SwapAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapAuditStatus) {
        if (!canMoveTo(next)) {
            error("swap audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round((maximum + graceDays.toDouble()) * 100) / 100

    fun snapshot(): SwapAudit {
        return SwapAudit(
            code = code,
            label = label,
            version = version,
            quantity = quantity,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            summary = summary,
            graceDays = graceDays,
            maximum = maximum,
            pinned = pinned,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap audit in status proposed. */
fun newSwapAudit(code: String, label: String): SwapAudit {
    return SwapAudit(
        code = code,
        label = label,
        version = 0,
        quantity = 0,
        externalRef = "",
        etaMinutes = 0,
        summary = "",
        graceDays = 0,
        maximum = 0.0,
        pinned = false,
        timezone = "",
        status = SwapAuditStatus.PROPOSED,
    )
}
