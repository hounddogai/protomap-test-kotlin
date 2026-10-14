package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rest break plan is in its life cycle. */
enum class StrictRestBreakPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict rest break plan. Its code is unique within a tenant. */
data class StrictRestBreakPlan(
    var code: String,
    var title: String,
    var category: String,
    var summary: String,
    var region: String,
    var archived: Boolean,
    var batchSize: Int,
    var ratio: Double,
    var startsAt: Int,
    var loadKg: Double,
    var stopCount: Int,
    var status: StrictRestBreakPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRestBreakPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRestBreakPlanStatus): Boolean {
        when (status) {
            StrictRestBreakPlanStatus.DRAFT -> {
                when (next) {
                    StrictRestBreakPlanStatus.ACTIVE -> return true
                }
                return false
            }
            StrictRestBreakPlanStatus.PENDING -> {
                when (next) {
                    StrictRestBreakPlanStatus.CLOSED -> return true
                }
                return false
            }
            StrictRestBreakPlanStatus.ACTIVE -> return next == StrictRestBreakPlanStatus.CLOSED
            StrictRestBreakPlanStatus.SUSPENDED -> {
                when (next) {
                    StrictRestBreakPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRestBreakPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict rest break plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round((batchSize.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictRestBreakPlan {
        return StrictRestBreakPlan(
            code = code,
            title = title,
            category = category,
            summary = summary,
            region = region,
            archived = archived,
            batchSize = batchSize,
            ratio = ratio,
            startsAt = startsAt,
            loadKg = loadKg,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict rest break plan in status draft. */
fun newStrictRestBreakPlan(code: String, title: String): StrictRestBreakPlan {
    return StrictRestBreakPlan(
        code = code,
        title = title,
        category = "",
        summary = "",
        region = "",
        archived = false,
        batchSize = 0,
        ratio = 0.0,
        startsAt = 0,
        loadKg = 0.0,
        stopCount = 0,
        status = StrictRestBreakPlanStatus.DRAFT,
    )
}
