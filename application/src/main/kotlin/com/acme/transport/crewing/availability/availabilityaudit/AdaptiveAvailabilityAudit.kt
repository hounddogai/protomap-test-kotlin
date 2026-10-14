package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive availability audit records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a adaptive availability audit is in its life cycle. */
enum class AdaptiveAvailabilityAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive availability audit, with what the planners need to act on it. */
data class AdaptiveAvailabilityAudit(
    var code: String,
    var label: String,
    var ceiling: Double,
    var visible: Boolean,
    var quantity: Int,
    var region: String,
    var status: AdaptiveAvailabilityAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAvailabilityAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAvailabilityAuditStatus): Boolean {
        when (status) {
            AdaptiveAvailabilityAuditStatus.NEW -> {
                when (next) {
                    AdaptiveAvailabilityAuditStatus.RUNNING, AdaptiveAvailabilityAuditStatus.DONE -> return true
                }
                return false
            }
            AdaptiveAvailabilityAuditStatus.QUEUED -> {
                when (next) {
                    AdaptiveAvailabilityAuditStatus.DONE, AdaptiveAvailabilityAuditStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveAvailabilityAuditStatus.RUNNING -> {
                when (next) {
                    AdaptiveAvailabilityAuditStatus.DONE, AdaptiveAvailabilityAuditStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveAvailabilityAuditStatus.DONE -> return next == AdaptiveAvailabilityAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveAvailabilityAuditStatus) {
        if (!canMoveTo(next)) {
            error("adaptive availability audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round(quantity.toDouble() * ceiling * 100) / 100

    fun snapshot(): AdaptiveAvailabilityAudit {
        return AdaptiveAvailabilityAudit(
            code = code,
            label = label,
            ceiling = ceiling,
            visible = visible,
            quantity = quantity,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive availability audit in status new. */
fun newAdaptiveAvailabilityAudit(code: String, label: String): AdaptiveAvailabilityAudit {
    return AdaptiveAvailabilityAudit(
        code = code,
        label = label,
        ceiling = 0.0,
        visible = false,
        quantity = 0,
        region = "",
        status = AdaptiveAvailabilityAuditStatus.NEW,
    )
}
