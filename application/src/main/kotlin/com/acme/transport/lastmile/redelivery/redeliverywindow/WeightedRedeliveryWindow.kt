package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted redelivery window records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a weighted redelivery window is in its life cycle. */
enum class WeightedRedeliveryWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted redelivery window. Its code is unique within a tenant. */
data class WeightedRedeliveryWindow(
    var code: String,
    var title: String,
    var retries: Int,
    var category: String,
    var reviewer: String,
    var score: Double,
    var priority: Int,
    var status: WeightedRedeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRedeliveryWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRedeliveryWindowStatus): Boolean {
        when (status) {
            WeightedRedeliveryWindowStatus.CREATED -> {
                when (next) {
                    WeightedRedeliveryWindowStatus.PUBLISHED, WeightedRedeliveryWindowStatus.RETIRED -> return true
                }
                return false
            }
            WeightedRedeliveryWindowStatus.VALIDATED -> return next == WeightedRedeliveryWindowStatus.PUBLISHED || next == WeightedRedeliveryWindowStatus.RETIRED
            WeightedRedeliveryWindowStatus.PUBLISHED -> {
                when (next) {
                    WeightedRedeliveryWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRedeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("weighted redelivery window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round((retries.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): WeightedRedeliveryWindow {
        return WeightedRedeliveryWindow(
            code = code,
            title = title,
            retries = retries,
            category = category,
            reviewer = reviewer,
            score = score,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted redelivery window in status created. */
fun newWeightedRedeliveryWindow(code: String, title: String): WeightedRedeliveryWindow {
    return WeightedRedeliveryWindow(
        code = code,
        title = title,
        retries = 0,
        category = "",
        reviewer = "",
        score = 0.0,
        priority = 0,
        status = WeightedRedeliveryWindowStatus.CREATED,
    )
}
