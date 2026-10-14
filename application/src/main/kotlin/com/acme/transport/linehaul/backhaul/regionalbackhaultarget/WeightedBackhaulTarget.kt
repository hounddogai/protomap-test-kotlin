package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.normalize

// The weighted backhaul target records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a weighted backhaul target is in its life cycle. */
enum class WeightedBackhaulTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted backhaul target. Its code is unique within a tenant. */
data class WeightedBackhaulTarget(
    var code: String,
    var name: String,
    var title: String,
    var pinned: Boolean,
    var source: String,
    var quantity: Int,
    var status: WeightedBackhaulTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBackhaulTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBackhaulTargetStatus): Boolean {
        when (status) {
            WeightedBackhaulTargetStatus.PLANNED -> return next == WeightedBackhaulTargetStatus.IN_PROGRESS
            WeightedBackhaulTargetStatus.SCHEDULED -> return next == WeightedBackhaulTargetStatus.CANCELLED
            WeightedBackhaulTargetStatus.IN_PROGRESS -> return next == WeightedBackhaulTargetStatus.COMPLETED || next == WeightedBackhaulTargetStatus.CANCELLED
            WeightedBackhaulTargetStatus.COMPLETED -> {
                when (next) {
                    WeightedBackhaulTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedBackhaulTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted backhaul target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): WeightedBackhaulTarget {
        return WeightedBackhaulTarget(
            code = code,
            name = name,
            title = title,
            pinned = pinned,
            source = source,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted backhaul target in status planned. */
fun newWeightedBackhaulTarget(code: String, name: String): WeightedBackhaulTarget {
    return WeightedBackhaulTarget(
        code = code,
        name = name,
        title = "",
        pinned = false,
        source = "",
        quantity = 0,
        status = WeightedBackhaulTargetStatus.PLANNED,
    )
}
