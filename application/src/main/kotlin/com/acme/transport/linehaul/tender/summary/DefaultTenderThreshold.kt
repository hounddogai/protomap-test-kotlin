package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tender threshold is in its life cycle. */
enum class DefaultTenderThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default tender threshold and its current status. */
data class DefaultTenderThreshold(
    var code: String,
    var label: String,
    var discount: Double,
    var batchSize: Int,
    var approved: Boolean,
    var budget: Double,
    var ratio: Double,
    var score: Double,
    var ceiling: Double,
    var summary: String,
    var locale: String,
    var status: DefaultTenderThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTenderThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTenderThresholdStatus): Boolean {
        when (status) {
            DefaultTenderThresholdStatus.PLANNED -> {
                when (next) {
                    DefaultTenderThresholdStatus.IN_PROGRESS, DefaultTenderThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultTenderThresholdStatus.SCHEDULED -> {
                when (next) {
                    DefaultTenderThresholdStatus.COMPLETED, DefaultTenderThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultTenderThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultTenderThresholdStatus.COMPLETED, DefaultTenderThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultTenderThresholdStatus.COMPLETED -> {
                when (next) {
                    DefaultTenderThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTenderThresholdStatus) {
        if (!canMoveTo(next)) {
            error("default tender threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((discount + budget) * 100) / 100

    fun snapshot(): DefaultTenderThreshold {
        return DefaultTenderThreshold(
            code = code,
            label = label,
            discount = discount,
            batchSize = batchSize,
            approved = approved,
            budget = budget,
            ratio = ratio,
            score = score,
            ceiling = ceiling,
            summary = summary,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default tender threshold in status planned. */
fun newDefaultTenderThreshold(code: String, label: String): DefaultTenderThreshold {
    return DefaultTenderThreshold(
        code = code,
        label = label,
        discount = 0.0,
        batchSize = 0,
        approved = false,
        budget = 0.0,
        ratio = 0.0,
        score = 0.0,
        ceiling = 0.0,
        summary = "",
        locale = "",
        status = DefaultTenderThresholdStatus.PLANNED,
    )
}
