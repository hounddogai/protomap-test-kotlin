package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback violation allocation is in its life cycle. */
enum class FallbackViolationAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback violation allocation and its current status. */
data class FallbackViolationAllocation(
    var code: String,
    var label: String,
    var contactEmail: String,
    var stopCount: Int,
    var tags: MutableList<String>,
    var currency: String,
    var etaMinutes: Int,
    var lane: String,
    var dock: String,
    var retries: Int,
    var status: FallbackViolationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackViolationAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackViolationAllocationStatus): Boolean {
        when (status) {
            FallbackViolationAllocationStatus.REQUESTED -> {
                when (next) {
                    FallbackViolationAllocationStatus.GRANTED, FallbackViolationAllocationStatus.REVOKED -> return true
                }
                return false
            }
            FallbackViolationAllocationStatus.GRANTED -> {
                when (next) {
                    FallbackViolationAllocationStatus.REVOKED, FallbackViolationAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackViolationAllocationStatus.REVOKED -> return next == FallbackViolationAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackViolationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fallback violation allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((retries.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackViolationAllocation {
        return FallbackViolationAllocation(
            code = code,
            label = label,
            contactEmail = contactEmail,
            stopCount = stopCount,
            tags = tags,
            currency = currency,
            etaMinutes = etaMinutes,
            lane = lane,
            dock = dock,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback violation allocation in status requested. */
fun newFallbackViolationAllocation(code: String, label: String): FallbackViolationAllocation {
    return FallbackViolationAllocation(
        code = code,
        label = label,
        contactEmail = "",
        stopCount = 0,
        tags = mutableListOf<String>(),
        currency = "",
        etaMinutes = 0,
        lane = "",
        dock = "",
        retries = 0,
        status = FallbackViolationAllocationStatus.REQUESTED,
    )
}
