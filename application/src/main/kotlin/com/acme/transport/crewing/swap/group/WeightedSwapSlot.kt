package com.acme.transport.crewing.swap.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted swap slot is in its life cycle. */
enum class WeightedSwapSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a weighted swap slot and its current status. */
data class WeightedSwapSlot(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var checksum: String,
    var version: Int,
    var status: WeightedSwapSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSwapSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSwapSlotStatus): Boolean {
        when (status) {
            WeightedSwapSlotStatus.CREATED -> return next == WeightedSwapSlotStatus.VALIDATED || next == WeightedSwapSlotStatus.PUBLISHED
            WeightedSwapSlotStatus.VALIDATED -> {
                when (next) {
                    WeightedSwapSlotStatus.RETIRED -> return true
                }
                return false
            }
            WeightedSwapSlotStatus.PUBLISHED -> return next == WeightedSwapSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedSwapSlotStatus) {
        if (!canMoveTo(next)) {
            error("weighted swap slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"weighted swap slot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((limit.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): WeightedSwapSlot {
        return WeightedSwapSlot(
            code = code,
            name = name,
            attributes = attributes,
            limit = limit,
            checksum = checksum,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted swap slot in status created. */
fun newWeightedSwapSlot(code: String, name: String): WeightedSwapSlot {
    return WeightedSwapSlot(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        checksum = "",
        version = 0,
        status = WeightedSwapSlotStatus.CREATED,
    )
}
