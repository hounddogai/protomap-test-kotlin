package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break policy records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break policy is in its life cycle. */
enum class RestBreakPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rest break policy and its current status. */
data class RestBreakPolicy(
    var code: String,
    var title: String,
    var budget: Double,
    var windowDays: Int,
    var quantity: Int,
    var channel: String,
    var createdAt: Int,
    var approved: Boolean,
    var description: String,
    var stopCount: Int,
    var pinned: Boolean,
    var status: RestBreakPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakPolicyStatus): Boolean {
        when (status) {
            RestBreakPolicyStatus.PLANNED -> {
                when (next) {
                    RestBreakPolicyStatus.IN_PROGRESS, RestBreakPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakPolicyStatus.SCHEDULED -> {
                when (next) {
                    RestBreakPolicyStatus.COMPLETED, RestBreakPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    RestBreakPolicyStatus.COMPLETED, RestBreakPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakPolicyStatus.COMPLETED -> return next == RestBreakPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RestBreakPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rest break policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((stopCount.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RestBreakPolicy {
        return RestBreakPolicy(
            code = code,
            title = title,
            budget = budget,
            windowDays = windowDays,
            quantity = quantity,
            channel = channel,
            createdAt = createdAt,
            approved = approved,
            description = description,
            stopCount = stopCount,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break policy in status planned. */
fun newRestBreakPolicy(code: String, title: String): RestBreakPolicy {
    return RestBreakPolicy(
        code = code,
        title = title,
        budget = 0.0,
        windowDays = 0,
        quantity = 0,
        channel = "",
        createdAt = 0,
        approved = false,
        description = "",
        stopCount = 0,
        pinned = false,
        status = RestBreakPolicyStatus.PLANNED,
    )
}
