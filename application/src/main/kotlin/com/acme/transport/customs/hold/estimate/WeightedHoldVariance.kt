package com.acme.transport.customs.hold.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hold variance is in its life cycle. */
enum class WeightedHoldVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted hold variance. Its code is unique within a tenant. */
data class WeightedHoldVariance(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var graceDays: Int,
    var amount: Double,
    var score: Double,
    var status: WeightedHoldVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHoldVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHoldVarianceStatus): Boolean {
        when (status) {
            WeightedHoldVarianceStatus.NEW -> return next == WeightedHoldVarianceStatus.QUEUED || next == WeightedHoldVarianceStatus.RUNNING
            WeightedHoldVarianceStatus.QUEUED -> return next == WeightedHoldVarianceStatus.RUNNING
            WeightedHoldVarianceStatus.RUNNING -> {
                when (next) {
                    WeightedHoldVarianceStatus.DONE, WeightedHoldVarianceStatus.FAILED -> return true
                }
                return false
            }
            WeightedHoldVarianceStatus.DONE -> {
                when (next) {
                    WeightedHoldVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedHoldVarianceStatus) {
        if (!canMoveTo(next)) {
            error("weighted hold variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(amount * graceDays.toDouble() * 100) / 100

    fun snapshot(): WeightedHoldVariance {
        return WeightedHoldVariance(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            graceDays = graceDays,
            amount = amount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted hold variance in status new. */
fun newWeightedHoldVariance(code: String, label: String): WeightedHoldVariance {
    return WeightedHoldVariance(
        code = code,
        label = label,
        etaMinutes = 0,
        graceDays = 0,
        amount = 0.0,
        score = 0.0,
        status = WeightedHoldVarianceStatus.NEW,
    )
}
