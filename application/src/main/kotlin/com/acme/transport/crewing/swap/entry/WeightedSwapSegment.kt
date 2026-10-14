package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted swap segment is in its life cycle. */
enum class WeightedSwapSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted swap segment and its current status. */
data class WeightedSwapSegment(
    var code: String,
    var name: String,
    var createdAt: Int,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var notes: String,
    var budget: Double,
    var ownerId: String,
    var revision: Int,
    var amount: Double,
    var status: WeightedSwapSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSwapSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSwapSegmentStatus): Boolean {
        when (status) {
            WeightedSwapSegmentStatus.PLANNED -> return next == WeightedSwapSegmentStatus.IN_PROGRESS
            WeightedSwapSegmentStatus.SCHEDULED -> return next == WeightedSwapSegmentStatus.IN_PROGRESS
            WeightedSwapSegmentStatus.IN_PROGRESS -> return next == WeightedSwapSegmentStatus.CANCELLED
            WeightedSwapSegmentStatus.COMPLETED -> {
                when (next) {
                    WeightedSwapSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedSwapSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted swap segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round(createdAt.toDouble() * budget * 100) / 100

    fun snapshot(): WeightedSwapSegment {
        return WeightedSwapSegment(
            code = code,
            name = name,
            createdAt = createdAt,
            unit = unit,
            attributes = attributes,
            capacity = capacity,
            notes = notes,
            budget = budget,
            ownerId = ownerId,
            revision = revision,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted swap segment in status planned. */
fun newWeightedSwapSegment(code: String, name: String): WeightedSwapSegment {
    return WeightedSwapSegment(
        code = code,
        name = name,
        createdAt = 0,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        notes = "",
        budget = 0.0,
        ownerId = "",
        revision = 0,
        amount = 0.0,
        status = WeightedSwapSegmentStatus.PLANNED,
    )
}
