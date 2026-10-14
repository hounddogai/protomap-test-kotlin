package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted lane plan bundle records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a weighted lane plan bundle is in its life cycle. */
enum class WeightedLanePlanBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted lane plan bundle as the lane plan team keeps it. */
data class WeightedLanePlanBundle(
    var code: String,
    var label: String,
    var carrier: String,
    var shiftCode: String,
    var dock: String,
    var sequence: Int,
    var stopCount: Int,
    var batchSize: Int,
    var limit: Int,
    var revision: Int,
    var status: WeightedLanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLanePlanBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLanePlanBundleStatus): Boolean {
        when (status) {
            WeightedLanePlanBundleStatus.PLANNED -> return next == WeightedLanePlanBundleStatus.IN_PROGRESS || next == WeightedLanePlanBundleStatus.COMPLETED
            WeightedLanePlanBundleStatus.SCHEDULED -> return next == WeightedLanePlanBundleStatus.IN_PROGRESS || next == WeightedLanePlanBundleStatus.COMPLETED
            WeightedLanePlanBundleStatus.IN_PROGRESS -> return next == WeightedLanePlanBundleStatus.CANCELLED
            WeightedLanePlanBundleStatus.COMPLETED -> return next == WeightedLanePlanBundleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedLanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("weighted lane plan bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"weighted lane plan bundle"} $code"

    fun projectedValue(): Double = round((sequence.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): WeightedLanePlanBundle {
        return WeightedLanePlanBundle(
            code = code,
            label = label,
            carrier = carrier,
            shiftCode = shiftCode,
            dock = dock,
            sequence = sequence,
            stopCount = stopCount,
            batchSize = batchSize,
            limit = limit,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted lane plan bundle in status planned. */
fun newWeightedLanePlanBundle(code: String, label: String): WeightedLanePlanBundle {
    return WeightedLanePlanBundle(
        code = code,
        label = label,
        carrier = "",
        shiftCode = "",
        dock = "",
        sequence = 0,
        stopCount = 0,
        batchSize = 0,
        limit = 0,
        revision = 0,
        status = WeightedLanePlanBundleStatus.PLANNED,
    )
}
