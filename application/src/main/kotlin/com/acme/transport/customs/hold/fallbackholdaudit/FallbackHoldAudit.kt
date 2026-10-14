package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback hold audit records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a fallback hold audit is in its life cycle. */
enum class FallbackHoldAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback hold audit and its current status. */
data class FallbackHoldAudit(
    var code: String,
    var title: String,
    var vehicleId: String,
    var pallets: Int,
    var version: Int,
    var status: FallbackHoldAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackHoldAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackHoldAuditStatus): Boolean {
        when (status) {
            FallbackHoldAuditStatus.CREATED -> return next == FallbackHoldAuditStatus.PUBLISHED || next == FallbackHoldAuditStatus.RETIRED
            FallbackHoldAuditStatus.VALIDATED -> {
                when (next) {
                    FallbackHoldAuditStatus.PUBLISHED, FallbackHoldAuditStatus.RETIRED -> return true
                }
                return false
            }
            FallbackHoldAuditStatus.PUBLISHED -> return next == FallbackHoldAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackHoldAuditStatus) {
        if (!canMoveTo(next)) {
            error("fallback hold audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((version.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): FallbackHoldAudit {
        return FallbackHoldAudit(
            code = code,
            title = title,
            vehicleId = vehicleId,
            pallets = pallets,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback hold audit in status created. */
fun newFallbackHoldAudit(code: String, title: String): FallbackHoldAudit {
    return FallbackHoldAudit(
        code = code,
        title = title,
        vehicleId = "",
        pallets = 0,
        version = 0,
        status = FallbackHoldAuditStatus.CREATED,
    )
}
