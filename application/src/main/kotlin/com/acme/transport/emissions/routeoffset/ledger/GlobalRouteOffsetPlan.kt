package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global route offset plan is in its life cycle. */
enum class GlobalRouteOffsetPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global route offset plan. Its code is unique within a tenant. */
data class GlobalRouteOffsetPlan(
    var code: String,
    var title: String,
    var endsAt: Int,
    var version: Int,
    var stopCount: Int,
    var source: String,
    var category: String,
    var summary: String,
    var carrier: String,
    var status: GlobalRouteOffsetPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRouteOffsetPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRouteOffsetPlanStatus): Boolean {
        when (status) {
            GlobalRouteOffsetPlanStatus.OPEN -> {
                when (next) {
                    GlobalRouteOffsetPlanStatus.RESOLVED, GlobalRouteOffsetPlanStatus.REOPENED -> return true
                }
                return false
            }
            GlobalRouteOffsetPlanStatus.IN_REVIEW -> return next == GlobalRouteOffsetPlanStatus.REOPENED || next == GlobalRouteOffsetPlanStatus.ARCHIVED
            GlobalRouteOffsetPlanStatus.RESOLVED -> return next == GlobalRouteOffsetPlanStatus.ARCHIVED
            GlobalRouteOffsetPlanStatus.REOPENED -> {
                when (next) {
                    GlobalRouteOffsetPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRouteOffsetPlanStatus) {
        if (!canMoveTo(next)) {
            error("global route offset plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((version.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalRouteOffsetPlan {
        return GlobalRouteOffsetPlan(
            code = code,
            title = title,
            endsAt = endsAt,
            version = version,
            stopCount = stopCount,
            source = source,
            category = category,
            summary = summary,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global route offset plan in status open. */
fun newGlobalRouteOffsetPlan(code: String, title: String): GlobalRouteOffsetPlan {
    return GlobalRouteOffsetPlan(
        code = code,
        title = title,
        endsAt = 0,
        version = 0,
        stopCount = 0,
        source = "",
        category = "",
        summary = "",
        carrier = "",
        status = GlobalRouteOffsetPlanStatus.OPEN,
    )
}
