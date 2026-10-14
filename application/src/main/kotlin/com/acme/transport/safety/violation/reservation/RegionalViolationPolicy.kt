package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional violation policy is in its life cycle. */
enum class RegionalViolationPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional violation policy and its current status. */
data class RegionalViolationPolicy(
    var code: String,
    var label: String,
    var createdAt: Int,
    var etaMinutes: Int,
    var capacity: Int,
    var pallets: Int,
    var source: String,
    var status: RegionalViolationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalViolationPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalViolationPolicyStatus): Boolean {
        when (status) {
            RegionalViolationPolicyStatus.CREATED -> return next == RegionalViolationPolicyStatus.VALIDATED || next == RegionalViolationPolicyStatus.RETIRED
            RegionalViolationPolicyStatus.VALIDATED -> {
                when (next) {
                    RegionalViolationPolicyStatus.RETIRED -> return true
                }
                return false
            }
            RegionalViolationPolicyStatus.PUBLISHED -> return next == RegionalViolationPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalViolationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("regional violation policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round((capacity.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalViolationPolicy {
        return RegionalViolationPolicy(
            code = code,
            label = label,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            capacity = capacity,
            pallets = pallets,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional violation policy in status created. */
fun newRegionalViolationPolicy(code: String, label: String): RegionalViolationPolicy {
    return RegionalViolationPolicy(
        code = code,
        label = label,
        createdAt = 0,
        etaMinutes = 0,
        capacity = 0,
        pallets = 0,
        source = "",
        status = RegionalViolationPolicyStatus.CREATED,
    )
}
