package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback hold policy is in its life cycle. */
enum class FallbackHoldPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback hold policy. Its code is unique within a tenant. */
data class FallbackHoldPolicy(
    var code: String,
    var title: String,
    var rate: Double,
    var lane: String,
    var windowDays: Int,
    var driverId: String,
    var category: String,
    var ceiling: Double,
    var version: Int,
    var unit: String,
    var approved: Boolean,
    var status: FallbackHoldPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackHoldPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackHoldPolicyStatus): Boolean {
        when (status) {
            FallbackHoldPolicyStatus.CREATED -> {
                when (next) {
                    FallbackHoldPolicyStatus.RETIRED -> return true
                }
                return false
            }
            FallbackHoldPolicyStatus.VALIDATED -> return next == FallbackHoldPolicyStatus.PUBLISHED || next == FallbackHoldPolicyStatus.RETIRED
            FallbackHoldPolicyStatus.PUBLISHED -> {
                when (next) {
                    FallbackHoldPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackHoldPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback hold policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((windowDays.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): FallbackHoldPolicy {
        return FallbackHoldPolicy(
            code = code,
            title = title,
            rate = rate,
            lane = lane,
            windowDays = windowDays,
            driverId = driverId,
            category = category,
            ceiling = ceiling,
            version = version,
            unit = unit,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback hold policy in status created. */
fun newFallbackHoldPolicy(code: String, title: String): FallbackHoldPolicy {
    return FallbackHoldPolicy(
        code = code,
        title = title,
        rate = 0.0,
        lane = "",
        windowDays = 0,
        driverId = "",
        category = "",
        ceiling = 0.0,
        version = 0,
        unit = "",
        approved = false,
        status = FallbackHoldPolicyStatus.CREATED,
    )
}
