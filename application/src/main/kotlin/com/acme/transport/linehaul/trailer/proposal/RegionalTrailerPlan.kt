package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional trailer plan is in its life cycle. */
enum class RegionalTrailerPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional trailer plan and its current status. */
data class RegionalTrailerPlan(
    var code: String,
    var title: String,
    var approved: Boolean,
    var updatedAt: Int,
    var checksum: String,
    var graceDays: Int,
    var category: String,
    var segment: String,
    var status: RegionalTrailerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerPlanStatus): Boolean {
        when (status) {
            RegionalTrailerPlanStatus.CREATED -> {
                when (next) {
                    RegionalTrailerPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalTrailerPlanStatus.VALIDATED -> return next == RegionalTrailerPlanStatus.RETIRED
            RegionalTrailerPlanStatus.PUBLISHED -> {
                when (next) {
                    RegionalTrailerPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional trailer plan"} $code"

    fun netVolume(): Double = round((graceDays.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalTrailerPlan {
        return RegionalTrailerPlan(
            code = code,
            title = title,
            approved = approved,
            updatedAt = updatedAt,
            checksum = checksum,
            graceDays = graceDays,
            category = category,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional trailer plan in status created. */
fun newRegionalTrailerPlan(code: String, title: String): RegionalTrailerPlan {
    return RegionalTrailerPlan(
        code = code,
        title = title,
        approved = false,
        updatedAt = 0,
        checksum = "",
        graceDays = 0,
        category = "",
        segment = "",
        status = RegionalTrailerPlanStatus.CREATED,
    )
}
