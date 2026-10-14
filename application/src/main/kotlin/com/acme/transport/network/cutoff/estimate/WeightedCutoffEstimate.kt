package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted cutoff estimate records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a weighted cutoff estimate is in its life cycle. */
enum class WeightedCutoffEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted cutoff estimate. Its code is unique within a tenant. */
data class WeightedCutoffEstimate(
    var code: String,
    var label: String,
    var approved: Boolean,
    var shiftCode: String,
    var startsAt: Int,
    var visible: Boolean,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var cost: Double,
    var status: WeightedCutoffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCutoffEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCutoffEstimateStatus): Boolean {
        when (status) {
            WeightedCutoffEstimateStatus.NEW -> return next == WeightedCutoffEstimateStatus.QUEUED
            WeightedCutoffEstimateStatus.QUEUED -> return next == WeightedCutoffEstimateStatus.FAILED
            WeightedCutoffEstimateStatus.RUNNING -> return next == WeightedCutoffEstimateStatus.DONE || next == WeightedCutoffEstimateStatus.FAILED
            WeightedCutoffEstimateStatus.DONE -> return next == WeightedCutoffEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedCutoffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("weighted cutoff estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((cost - startsAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedCutoffEstimate {
        return WeightedCutoffEstimate(
            code = code,
            label = label,
            approved = approved,
            shiftCode = shiftCode,
            startsAt = startsAt,
            visible = visible,
            pinned = pinned,
            tags = tags,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted cutoff estimate in status new. */
fun newWeightedCutoffEstimate(code: String, label: String): WeightedCutoffEstimate {
    return WeightedCutoffEstimate(
        code = code,
        label = label,
        approved = false,
        shiftCode = "",
        startsAt = 0,
        visible = false,
        pinned = false,
        tags = mutableListOf<String>(),
        cost = 0.0,
        status = WeightedCutoffEstimateStatus.NEW,
    )
}
