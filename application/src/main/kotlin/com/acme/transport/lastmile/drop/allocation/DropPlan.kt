package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop plan is in its life cycle. */
enum class DropPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A drop plan. Its code is unique within a tenant. */
data class DropPlan(
    var code: String,
    var title: String,
    var region: String,
    var endsAt: Int,
    var windowDays: Int,
    var timezone: String,
    var country: String,
    var startsAt: Int,
    var visible: Boolean,
    var archived: Boolean,
    var fuelLiters: Double,
    var status: DropPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropPlanStatus): Boolean {
        when (status) {
            DropPlanStatus.DRAFT -> return next == DropPlanStatus.ACTIVE || next == DropPlanStatus.CLOSED
            DropPlanStatus.PENDING -> {
                when (next) {
                    DropPlanStatus.SUSPENDED -> return true
                }
                return false
            }
            DropPlanStatus.ACTIVE -> {
                when (next) {
                    DropPlanStatus.SUSPENDED -> return true
                }
                return false
            }
            DropPlanStatus.SUSPENDED -> {
                when (next) {
                    DropPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropPlanStatus) {
        if (!canMoveTo(next)) {
            error("drop plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round(fuelLiters * endsAt.toDouble() * 100) / 100

    fun snapshot(): DropPlan {
        return DropPlan(
            code = code,
            title = title,
            region = region,
            endsAt = endsAt,
            windowDays = windowDays,
            timezone = timezone,
            country = country,
            startsAt = startsAt,
            visible = visible,
            archived = archived,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a drop plan in status draft. */
fun newDropPlan(code: String, title: String): DropPlan {
    return DropPlan(
        code = code,
        title = title,
        region = "",
        endsAt = 0,
        windowDays = 0,
        timezone = "",
        country = "",
        startsAt = 0,
        visible = false,
        archived = false,
        fuelLiters = 0.0,
        status = DropPlanStatus.DRAFT,
    )
}
