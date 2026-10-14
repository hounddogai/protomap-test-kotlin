package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation plan records of safety's violation work, and the rules, stores, and calculations around them.

/** Where a violation plan is in its life cycle. */
enum class ViolationPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a violation plan and its current status. */
data class ViolationPlan(
    var code: String,
    var label: String,
    var timezone: String,
    var externalRef: String,
    var updatedAt: Int,
    var visible: Boolean,
    var distanceKm: Double,
    var retries: Int,
    var source: String,
    var approved: Boolean,
    var pinned: Boolean,
    var status: ViolationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationPlanStatus): Boolean {
        when (status) {
            ViolationPlanStatus.DRAFT -> {
                when (next) {
                    ViolationPlanStatus.PENDING, ViolationPlanStatus.CLOSED -> return true
                }
                return false
            }
            ViolationPlanStatus.PENDING -> {
                when (next) {
                    ViolationPlanStatus.SUSPENDED, ViolationPlanStatus.CLOSED -> return true
                }
                return false
            }
            ViolationPlanStatus.ACTIVE -> return next == ViolationPlanStatus.SUSPENDED || next == ViolationPlanStatus.CLOSED
            ViolationPlanStatus.SUSPENDED -> return next == ViolationPlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ViolationPlanStatus) {
        if (!canMoveTo(next)) {
            error("violation plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(distanceKm * updatedAt.toDouble() * 100) / 100

    fun snapshot(): ViolationPlan {
        return ViolationPlan(
            code = code,
            label = label,
            timezone = timezone,
            externalRef = externalRef,
            updatedAt = updatedAt,
            visible = visible,
            distanceKm = distanceKm,
            retries = retries,
            source = source,
            approved = approved,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a violation plan in status draft. */
fun newViolationPlan(code: String, label: String): ViolationPlan {
    return ViolationPlan(
        code = code,
        label = label,
        timezone = "",
        externalRef = "",
        updatedAt = 0,
        visible = false,
        distanceKm = 0.0,
        retries = 0,
        source = "",
        approved = false,
        pinned = false,
        status = ViolationPlanStatus.DRAFT,
    )
}
