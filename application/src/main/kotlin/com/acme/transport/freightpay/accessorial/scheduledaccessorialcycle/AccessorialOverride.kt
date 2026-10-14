package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial override is in its life cycle. */
enum class AccessorialOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A accessorial override. Its code is unique within a tenant. */
data class AccessorialOverride(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var threshold: Double,
    var etaMinutes: Int,
    var status: AccessorialOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialOverrideStatus): Boolean {
        when (status) {
            AccessorialOverrideStatus.DRAFT -> return next == AccessorialOverrideStatus.PENDING || next == AccessorialOverrideStatus.ACTIVE
            AccessorialOverrideStatus.PENDING -> return next == AccessorialOverrideStatus.ACTIVE
            AccessorialOverrideStatus.ACTIVE -> return next == AccessorialOverrideStatus.SUSPENDED
            AccessorialOverrideStatus.SUSPENDED -> {
                when (next) {
                    AccessorialOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialOverrideStatus) {
        if (!canMoveTo(next)) {
            error("accessorial override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"accessorial override"} $code"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() + threshold) * 100) / 100

    fun snapshot(): AccessorialOverride {
        return AccessorialOverride(
            code = code,
            title = title,
            pinned = pinned,
            threshold = threshold,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial override in status draft. */
fun newAccessorialOverride(code: String, title: String): AccessorialOverride {
    return AccessorialOverride(
        code = code,
        title = title,
        pinned = false,
        threshold = 0.0,
        etaMinutes = 0,
        status = AccessorialOverrideStatus.DRAFT,
    )
}
