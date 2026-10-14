package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted drop target records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a weighted drop target is in its life cycle. */
enum class WeightedDropTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted drop target, with what the planners need to act on it. */
data class WeightedDropTarget(
    var code: String,
    var label: String,
    var contactEmail: String,
    var ratio: Double,
    var channel: String,
    var minimum: Double,
    var carrier: String,
    var region: String,
    var externalRef: String,
    var etaMinutes: Int,
    var status: WeightedDropTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDropTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDropTargetStatus): Boolean {
        when (status) {
            WeightedDropTargetStatus.CREATED -> return next == WeightedDropTargetStatus.PUBLISHED
            WeightedDropTargetStatus.VALIDATED -> return next == WeightedDropTargetStatus.PUBLISHED || next == WeightedDropTargetStatus.RETIRED
            WeightedDropTargetStatus.PUBLISHED -> return next == WeightedDropTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedDropTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted drop target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted drop target"} $code"

    fun projectedLoad(): Double = round(minimum * ratio * 100) / 100

    fun snapshot(): WeightedDropTarget {
        return WeightedDropTarget(
            code = code,
            label = label,
            contactEmail = contactEmail,
            ratio = ratio,
            channel = channel,
            minimum = minimum,
            carrier = carrier,
            region = region,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted drop target in status created. */
fun newWeightedDropTarget(code: String, label: String): WeightedDropTarget {
    return WeightedDropTarget(
        code = code,
        label = label,
        contactEmail = "",
        ratio = 0.0,
        channel = "",
        minimum = 0.0,
        carrier = "",
        region = "",
        externalRef = "",
        etaMinutes = 0,
        status = WeightedDropTargetStatus.CREATED,
    )
}
