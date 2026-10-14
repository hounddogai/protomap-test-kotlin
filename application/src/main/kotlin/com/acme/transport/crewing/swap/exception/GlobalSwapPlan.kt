package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global swap plan is in its life cycle. */
enum class GlobalSwapPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global swap plan as the swap team keeps it. */
data class GlobalSwapPlan(
    var code: String,
    var name: String,
    var attempts: Int,
    var retries: Int,
    var segment: String,
    var tags: MutableList<String>,
    var locale: String,
    var visible: Boolean,
    var status: GlobalSwapPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSwapPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSwapPlanStatus): Boolean {
        when (status) {
            GlobalSwapPlanStatus.REQUESTED -> {
                when (next) {
                    GlobalSwapPlanStatus.GRANTED, GlobalSwapPlanStatus.REVOKED -> return true
                }
                return false
            }
            GlobalSwapPlanStatus.GRANTED -> return next == GlobalSwapPlanStatus.REVOKED
            GlobalSwapPlanStatus.REVOKED -> return next == GlobalSwapPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalSwapPlanStatus) {
        if (!canMoveTo(next)) {
            error("global swap plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global swap plan"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((retries.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): GlobalSwapPlan {
        return GlobalSwapPlan(
            code = code,
            name = name,
            attempts = attempts,
            retries = retries,
            segment = segment,
            tags = tags,
            locale = locale,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global swap plan in status requested. */
fun newGlobalSwapPlan(code: String, name: String): GlobalSwapPlan {
    return GlobalSwapPlan(
        code = code,
        name = name,
        attempts = 0,
        retries = 0,
        segment = "",
        tags = mutableListOf<String>(),
        locale = "",
        visible = false,
        status = GlobalSwapPlanStatus.REQUESTED,
    )
}
