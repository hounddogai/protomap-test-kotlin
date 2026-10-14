package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient hub plan records of network's hub work, and the rules, stores, and calculations around them.

/** Where a lenient hub plan is in its life cycle. */
enum class LenientHubPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient hub plan as the hub team keeps it. */
data class LenientHubPlan(
    var code: String,
    var title: String,
    var maximum: Double,
    var category: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var status: LenientHubPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHubPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHubPlanStatus): Boolean {
        when (status) {
            LenientHubPlanStatus.PROPOSED -> return next == LenientHubPlanStatus.WITHDRAWN
            LenientHubPlanStatus.APPROVED -> return next == LenientHubPlanStatus.REJECTED
            LenientHubPlanStatus.REJECTED -> {
                when (next) {
                    LenientHubPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientHubPlanStatus) {
        if (!canMoveTo(next)) {
            error("lenient hub plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient hub plan"} $code"

    fun netVolume(): Double = round((loadKg + maximum) * 100) / 100

    fun snapshot(): LenientHubPlan {
        return LenientHubPlan(
            code = code,
            title = title,
            maximum = maximum,
            category = category,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient hub plan in status proposed. */
fun newLenientHubPlan(code: String, title: String): LenientHubPlan {
    return LenientHubPlan(
        code = code,
        title = title,
        maximum = 0.0,
        category = "",
        durationMinutes = 0,
        loadKg = 0.0,
        status = LenientHubPlanStatus.PROPOSED,
    )
}
