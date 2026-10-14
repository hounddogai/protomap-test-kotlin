package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global camera event budget is in its life cycle. */
enum class GlobalCameraEventBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a global camera event budget and its current status. */
data class GlobalCameraEventBudget(
    var code: String,
    var label: String,
    var name: String,
    var createdAt: Int,
    var country: String,
    var limit: Int,
    var trailerId: String,
    var status: GlobalCameraEventBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCameraEventBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCameraEventBudgetStatus): Boolean {
        when (status) {
            GlobalCameraEventBudgetStatus.CREATED -> return next == GlobalCameraEventBudgetStatus.VALIDATED || next == GlobalCameraEventBudgetStatus.PUBLISHED
            GlobalCameraEventBudgetStatus.VALIDATED -> {
                when (next) {
                    GlobalCameraEventBudgetStatus.PUBLISHED, GlobalCameraEventBudgetStatus.RETIRED -> return true
                }
                return false
            }
            GlobalCameraEventBudgetStatus.PUBLISHED -> return next == GlobalCameraEventBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalCameraEventBudgetStatus) {
        if (!canMoveTo(next)) {
            error("global camera event budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((createdAt.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): GlobalCameraEventBudget {
        return GlobalCameraEventBudget(
            code = code,
            label = label,
            name = name,
            createdAt = createdAt,
            country = country,
            limit = limit,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global camera event budget in status created. */
fun newGlobalCameraEventBudget(code: String, label: String): GlobalCameraEventBudget {
    return GlobalCameraEventBudget(
        code = code,
        label = label,
        name = "",
        createdAt = 0,
        country = "",
        limit = 0,
        trailerId = "",
        status = GlobalCameraEventBudgetStatus.CREATED,
    )
}
