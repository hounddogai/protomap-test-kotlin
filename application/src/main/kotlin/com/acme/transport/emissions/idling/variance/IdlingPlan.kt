package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling plan is in its life cycle. */
enum class IdlingPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A idling plan. Its code is unique within a tenant. */
data class IdlingPlan(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var threshold: Double,
    var ceiling: Double,
    var category: String,
    var status: IdlingPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingPlanStatus): Boolean {
        when (status) {
            IdlingPlanStatus.IDLE -> return next == IdlingPlanStatus.READY || next == IdlingPlanStatus.STOPPED
            IdlingPlanStatus.WARMING -> return next == IdlingPlanStatus.STOPPED
            IdlingPlanStatus.READY -> return next == IdlingPlanStatus.STOPPED
            IdlingPlanStatus.DRAINING -> {
                when (next) {
                    IdlingPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingPlanStatus) {
        if (!canMoveTo(next)) {
            error("idling plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"idling plan"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((ceiling - limit.toDouble()) * 100) / 100

    fun snapshot(): IdlingPlan {
        return IdlingPlan(
            code = code,
            title = title,
            attributes = attributes,
            limit = limit,
            threshold = threshold,
            ceiling = ceiling,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling plan in status idle. */
fun newIdlingPlan(code: String, title: String): IdlingPlan {
    return IdlingPlan(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        threshold = 0.0,
        ceiling = 0.0,
        category = "",
        status = IdlingPlanStatus.IDLE,
    )
}
