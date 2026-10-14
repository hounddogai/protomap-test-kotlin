package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict violation policy records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a strict violation policy is in its life cycle. */
enum class StrictViolationPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict violation policy and its current status. */
data class StrictViolationPolicy(
    var code: String,
    var name: String,
    var windowDays: Int,
    var ownerId: String,
    var endsAt: Int,
    var notes: String,
    var createdAt: Int,
    var externalRef: String,
    var expiresAt: Int,
    var tags: MutableList<String>,
    var status: StrictViolationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictViolationPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictViolationPolicyStatus): Boolean {
        when (status) {
            StrictViolationPolicyStatus.OPEN -> {
                when (next) {
                    StrictViolationPolicyStatus.RESOLVED -> return true
                }
                return false
            }
            StrictViolationPolicyStatus.IN_REVIEW -> {
                when (next) {
                    StrictViolationPolicyStatus.RESOLVED -> return true
                }
                return false
            }
            StrictViolationPolicyStatus.RESOLVED -> return next == StrictViolationPolicyStatus.REOPENED || next == StrictViolationPolicyStatus.ARCHIVED
            StrictViolationPolicyStatus.REOPENED -> return next == StrictViolationPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictViolationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict violation policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict violation policy"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((windowDays.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): StrictViolationPolicy {
        return StrictViolationPolicy(
            code = code,
            name = name,
            windowDays = windowDays,
            ownerId = ownerId,
            endsAt = endsAt,
            notes = notes,
            createdAt = createdAt,
            externalRef = externalRef,
            expiresAt = expiresAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict violation policy in status open. */
fun newStrictViolationPolicy(code: String, name: String): StrictViolationPolicy {
    return StrictViolationPolicy(
        code = code,
        name = name,
        windowDays = 0,
        ownerId = "",
        endsAt = 0,
        notes = "",
        createdAt = 0,
        externalRef = "",
        expiresAt = 0,
        tags = mutableListOf<String>(),
        status = StrictViolationPolicyStatus.OPEN,
    )
}
