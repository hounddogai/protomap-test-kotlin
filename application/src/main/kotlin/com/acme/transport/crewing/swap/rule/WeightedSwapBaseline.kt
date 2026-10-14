package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted swap baseline is in its life cycle. */
enum class WeightedSwapBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted swap baseline. Its code is unique within a tenant. */
data class WeightedSwapBaseline(
    var code: String,
    var name: String,
    var loadKg: Double,
    var amount: Double,
    var category: String,
    var graceDays: Int,
    var retries: Int,
    var revision: Int,
    var phone: String,
    var label: String,
    var status: WeightedSwapBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSwapBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSwapBaselineStatus): Boolean {
        when (status) {
            WeightedSwapBaselineStatus.PLANNED -> return next == WeightedSwapBaselineStatus.COMPLETED || next == WeightedSwapBaselineStatus.CANCELLED
            WeightedSwapBaselineStatus.SCHEDULED -> return next == WeightedSwapBaselineStatus.IN_PROGRESS || next == WeightedSwapBaselineStatus.COMPLETED
            WeightedSwapBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedSwapBaselineStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedSwapBaselineStatus.COMPLETED -> {
                when (next) {
                    WeightedSwapBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedSwapBaselineStatus) {
        if (!canMoveTo(next)) {
            error("weighted swap baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$name]"

    fun netValue(): Double = round((loadKg + retries.toDouble()) * 100) / 100

    fun snapshot(): WeightedSwapBaseline {
        return WeightedSwapBaseline(
            code = code,
            name = name,
            loadKg = loadKg,
            amount = amount,
            category = category,
            graceDays = graceDays,
            retries = retries,
            revision = revision,
            phone = phone,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted swap baseline in status planned. */
fun newWeightedSwapBaseline(code: String, name: String): WeightedSwapBaseline {
    return WeightedSwapBaseline(
        code = code,
        name = name,
        loadKg = 0.0,
        amount = 0.0,
        category = "",
        graceDays = 0,
        retries = 0,
        revision = 0,
        phone = "",
        label = "",
        status = WeightedSwapBaselineStatus.PLANNED,
    )
}
