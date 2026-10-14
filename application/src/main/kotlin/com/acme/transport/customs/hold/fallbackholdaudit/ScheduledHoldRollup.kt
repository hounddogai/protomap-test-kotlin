package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize

/** Where a scheduled hold rollup is in its life cycle. */
enum class ScheduledHoldRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled hold rollup as the hold team keeps it. */
data class ScheduledHoldRollup(
    var code: String,
    var title: String,
    var budget: Double,
    var category: String,
    var source: String,
    var status: ScheduledHoldRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldRollupStatus): Boolean {
        when (status) {
            ScheduledHoldRollupStatus.CREATED -> {
                when (next) {
                    ScheduledHoldRollupStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledHoldRollupStatus.VALIDATED -> return next == ScheduledHoldRollupStatus.PUBLISHED || next == ScheduledHoldRollupStatus.RETIRED
            ScheduledHoldRollupStatus.PUBLISHED -> return next == ScheduledHoldRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledHoldRollupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = budget

    fun snapshot(): ScheduledHoldRollup {
        return ScheduledHoldRollup(
            code = code,
            title = title,
            budget = budget,
            category = category,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled hold rollup in status created. */
fun newScheduledHoldRollup(code: String, title: String): ScheduledHoldRollup {
    return ScheduledHoldRollup(
        code = code,
        title = title,
        budget = 0.0,
        category = "",
        source = "",
        status = ScheduledHoldRollupStatus.CREATED,
    )
}
