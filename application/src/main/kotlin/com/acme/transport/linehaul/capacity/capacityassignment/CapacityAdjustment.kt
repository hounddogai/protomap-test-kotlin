package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity adjustment is in its life cycle. */
enum class CapacityAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capacity adjustment and its current status. */
data class CapacityAdjustment(
    var code: String,
    var title: String,
    var trailerId: String,
    var pallets: Int,
    var version: Int,
    var source: String,
    var revision: Int,
    var retries: Int,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var status: CapacityAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityAdjustmentStatus): Boolean {
        when (status) {
            CapacityAdjustmentStatus.PLANNED -> {
                when (next) {
                    CapacityAdjustmentStatus.SCHEDULED, CapacityAdjustmentStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CapacityAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    CapacityAdjustmentStatus.IN_PROGRESS, CapacityAdjustmentStatus.COMPLETED -> return true
                }
                return false
            }
            CapacityAdjustmentStatus.IN_PROGRESS -> return next == CapacityAdjustmentStatus.COMPLETED
            CapacityAdjustmentStatus.COMPLETED -> {
                when (next) {
                    CapacityAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("capacity adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((pallets.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): CapacityAdjustment {
        return CapacityAdjustment(
            code = code,
            title = title,
            trailerId = trailerId,
            pallets = pallets,
            version = version,
            source = source,
            revision = revision,
            retries = retries,
            attributes = attributes,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capacity adjustment in status planned. */
fun newCapacityAdjustment(code: String, title: String): CapacityAdjustment {
    return CapacityAdjustment(
        code = code,
        title = title,
        trailerId = "",
        pallets = 0,
        version = 0,
        source = "",
        revision = 0,
        retries = 0,
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        status = CapacityAdjustmentStatus.PLANNED,
    )
}
