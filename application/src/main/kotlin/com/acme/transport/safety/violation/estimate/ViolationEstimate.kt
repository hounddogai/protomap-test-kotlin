package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation estimate records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation estimate is in its life cycle. */
enum class ViolationEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a violation estimate and its current status. */
data class ViolationEstimate(
    var code: String,
    var name: String,
    var quantity: Int,
    var etaMinutes: Int,
    var region: String,
    var priority: Int,
    var active: Boolean,
    var channel: String,
    var description: String,
    var windowDays: Int,
    var limit: Int,
    var status: ViolationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationEstimateStatus): Boolean {
        when (status) {
            ViolationEstimateStatus.OPEN -> return next == ViolationEstimateStatus.RESOLVED || next == ViolationEstimateStatus.REOPENED
            ViolationEstimateStatus.IN_REVIEW -> return next == ViolationEstimateStatus.REOPENED || next == ViolationEstimateStatus.ARCHIVED
            ViolationEstimateStatus.RESOLVED -> return next == ViolationEstimateStatus.REOPENED
            ViolationEstimateStatus.REOPENED -> return next == ViolationEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ViolationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("violation estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round(etaMinutes.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): ViolationEstimate {
        return ViolationEstimate(
            code = code,
            name = name,
            quantity = quantity,
            etaMinutes = etaMinutes,
            region = region,
            priority = priority,
            active = active,
            channel = channel,
            description = description,
            windowDays = windowDays,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a violation estimate in status open. */
fun newViolationEstimate(code: String, name: String): ViolationEstimate {
    return ViolationEstimate(
        code = code,
        name = name,
        quantity = 0,
        etaMinutes = 0,
        region = "",
        priority = 0,
        active = false,
        channel = "",
        description = "",
        windowDays = 0,
        limit = 0,
        status = ViolationEstimateStatus.OPEN,
    )
}
