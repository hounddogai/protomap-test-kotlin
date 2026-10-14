package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul variance records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul variance is in its life cycle. */
enum class BackhaulVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a backhaul variance and its current status. */
data class BackhaulVariance(
    var code: String,
    var title: String,
    var stopCount: Int,
    var quantity: Int,
    var minimum: Double,
    var category: String,
    var amount: Double,
    var status: BackhaulVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulVarianceStatus): Boolean {
        when (status) {
            BackhaulVarianceStatus.NEW -> return next == BackhaulVarianceStatus.RUNNING || next == BackhaulVarianceStatus.FAILED
            BackhaulVarianceStatus.QUEUED -> return next == BackhaulVarianceStatus.DONE || next == BackhaulVarianceStatus.FAILED
            BackhaulVarianceStatus.RUNNING -> {
                when (next) {
                    BackhaulVarianceStatus.DONE, BackhaulVarianceStatus.FAILED -> return true
                }
                return false
            }
            BackhaulVarianceStatus.DONE -> return next == BackhaulVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BackhaulVarianceStatus) {
        if (!canMoveTo(next)) {
            error("backhaul variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((quantity.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): BackhaulVariance {
        return BackhaulVariance(
            code = code,
            title = title,
            stopCount = stopCount,
            quantity = quantity,
            minimum = minimum,
            category = category,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a backhaul variance in status new. */
fun newBackhaulVariance(code: String, title: String): BackhaulVariance {
    return BackhaulVariance(
        code = code,
        title = title,
        stopCount = 0,
        quantity = 0,
        minimum = 0.0,
        category = "",
        amount = 0.0,
        status = BackhaulVarianceStatus.NEW,
    )
}
