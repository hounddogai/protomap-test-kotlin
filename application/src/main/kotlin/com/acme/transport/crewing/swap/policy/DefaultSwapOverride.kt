package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default swap override is in its life cycle. */
enum class DefaultSwapOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default swap override. Its code is unique within a tenant. */
data class DefaultSwapOverride(
    var code: String,
    var title: String,
    var trailerId: String,
    var locale: String,
    var routeCode: String,
    var graceDays: Int,
    var updatedAt: Int,
    var status: DefaultSwapOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultSwapOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultSwapOverrideStatus): Boolean {
        when (status) {
            DefaultSwapOverrideStatus.OPEN -> {
                when (next) {
                    DefaultSwapOverrideStatus.RESOLVED, DefaultSwapOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultSwapOverrideStatus.IN_REVIEW -> {
                when (next) {
                    DefaultSwapOverrideStatus.RESOLVED, DefaultSwapOverrideStatus.REOPENED -> return true
                }
                return false
            }
            DefaultSwapOverrideStatus.RESOLVED -> {
                when (next) {
                    DefaultSwapOverrideStatus.REOPENED, DefaultSwapOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultSwapOverrideStatus.REOPENED -> {
                when (next) {
                    DefaultSwapOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultSwapOverrideStatus) {
        if (!canMoveTo(next)) {
            error("default swap override $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default swap override"} $code"

    fun adjustedLoad(): Double = round((graceDays.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultSwapOverride {
        return DefaultSwapOverride(
            code = code,
            title = title,
            trailerId = trailerId,
            locale = locale,
            routeCode = routeCode,
            graceDays = graceDays,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default swap override in status open. */
fun newDefaultSwapOverride(code: String, title: String): DefaultSwapOverride {
    return DefaultSwapOverride(
        code = code,
        title = title,
        trailerId = "",
        locale = "",
        routeCode = "",
        graceDays = 0,
        updatedAt = 0,
        status = DefaultSwapOverrideStatus.OPEN,
    )
}
