package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire budget is in its life cycle. */
enum class TireBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tire budget. Its code is unique within a tenant. */
data class TireBudget(
    var code: String,
    var title: String,
    var lane: String,
    var loadKg: Double,
    var depot: String,
    var tags: MutableList<String>,
    var category: String,
    var priority: Int,
    var status: TireBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireBudgetStatus): Boolean {
        when (status) {
            TireBudgetStatus.CREATED -> {
                when (next) {
                    TireBudgetStatus.VALIDATED, TireBudgetStatus.RETIRED -> return true
                }
                return false
            }
            TireBudgetStatus.VALIDATED -> return next == TireBudgetStatus.PUBLISHED || next == TireBudgetStatus.RETIRED
            TireBudgetStatus.PUBLISHED -> {
                when (next) {
                    TireBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tire budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tire budget"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((loadKg + priority.toDouble()) * 100) / 100

    fun snapshot(): TireBudget {
        return TireBudget(
            code = code,
            title = title,
            lane = lane,
            loadKg = loadKg,
            depot = depot,
            tags = tags,
            category = category,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tire budget in status created. */
fun newTireBudget(code: String, title: String): TireBudget {
    return TireBudget(
        code = code,
        title = title,
        lane = "",
        loadKg = 0.0,
        depot = "",
        tags = mutableListOf<String>(),
        category = "",
        priority = 0,
        status = TireBudgetStatus.CREATED,
    )
}
