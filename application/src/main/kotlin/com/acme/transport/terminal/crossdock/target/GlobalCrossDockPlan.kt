package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cross dock plan is in its life cycle. */
enum class GlobalCrossDockPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global cross dock plan as the cross dock team keeps it. */
data class GlobalCrossDockPlan(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var weight: Double,
    var updatedAt: Int,
    var durationMinutes: Int,
    var endsAt: Int,
    var status: GlobalCrossDockPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCrossDockPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCrossDockPlanStatus): Boolean {
        when (status) {
            GlobalCrossDockPlanStatus.DRAFT -> return next == GlobalCrossDockPlanStatus.PENDING || next == GlobalCrossDockPlanStatus.SUSPENDED
            GlobalCrossDockPlanStatus.PENDING -> return next == GlobalCrossDockPlanStatus.ACTIVE || next == GlobalCrossDockPlanStatus.CLOSED
            GlobalCrossDockPlanStatus.ACTIVE -> return next == GlobalCrossDockPlanStatus.SUSPENDED || next == GlobalCrossDockPlanStatus.CLOSED
            GlobalCrossDockPlanStatus.SUSPENDED -> {
                when (next) {
                    GlobalCrossDockPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCrossDockPlanStatus) {
        if (!canMoveTo(next)) {
            error("global cross dock plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global cross dock plan"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((durationMinutes.toDouble() + weight) * 100) / 100

    fun snapshot(): GlobalCrossDockPlan {
        return GlobalCrossDockPlan(
            code = code,
            label = label,
            attributes = attributes,
            weight = weight,
            updatedAt = updatedAt,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global cross dock plan in status draft. */
fun newGlobalCrossDockPlan(code: String, label: String): GlobalCrossDockPlan {
    return GlobalCrossDockPlan(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        weight = 0.0,
        updatedAt = 0,
        durationMinutes = 0,
        endsAt = 0,
        status = GlobalCrossDockPlanStatus.DRAFT,
    )
}
