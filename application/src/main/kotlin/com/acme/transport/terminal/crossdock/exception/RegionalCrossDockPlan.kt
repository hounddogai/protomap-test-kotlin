package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cross dock plan is in its life cycle. */
enum class RegionalCrossDockPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional cross dock plan. Its code is unique within a tenant. */
data class RegionalCrossDockPlan(
    var code: String,
    var title: String,
    var timezone: String,
    var quantity: Int,
    var locale: String,
    var createdAt: Int,
    var phone: String,
    var retries: Int,
    var category: String,
    var version: Int,
    var status: RegionalCrossDockPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCrossDockPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCrossDockPlanStatus): Boolean {
        when (status) {
            RegionalCrossDockPlanStatus.PROPOSED -> {
                when (next) {
                    RegionalCrossDockPlanStatus.REJECTED -> return true
                }
                return false
            }
            RegionalCrossDockPlanStatus.APPROVED -> {
                when (next) {
                    RegionalCrossDockPlanStatus.REJECTED, RegionalCrossDockPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalCrossDockPlanStatus.REJECTED -> return next == RegionalCrossDockPlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalCrossDockPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional cross dock plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round((version.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalCrossDockPlan {
        return RegionalCrossDockPlan(
            code = code,
            title = title,
            timezone = timezone,
            quantity = quantity,
            locale = locale,
            createdAt = createdAt,
            phone = phone,
            retries = retries,
            category = category,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional cross dock plan in status proposed. */
fun newRegionalCrossDockPlan(code: String, title: String): RegionalCrossDockPlan {
    return RegionalCrossDockPlan(
        code = code,
        title = title,
        timezone = "",
        quantity = 0,
        locale = "",
        createdAt = 0,
        phone = "",
        retries = 0,
        category = "",
        version = 0,
        status = RegionalCrossDockPlanStatus.PROPOSED,
    )
}
