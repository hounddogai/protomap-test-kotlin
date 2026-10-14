package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending backhaul mapping is in its life cycle. */
enum class PendingBackhaulMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending backhaul mapping, with what the planners need to act on it. */
data class PendingBackhaulMapping(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var priority: Int,
    var cost: Double,
    var pallets: Int,
    var capacity: Int,
    var fuelLiters: Double,
    var attributes: MutableMap<String, String>,
    var status: PendingBackhaulMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBackhaulMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBackhaulMappingStatus): Boolean {
        when (status) {
            PendingBackhaulMappingStatus.REQUESTED -> return next == PendingBackhaulMappingStatus.GRANTED || next == PendingBackhaulMappingStatus.EXPIRED
            PendingBackhaulMappingStatus.GRANTED -> return next == PendingBackhaulMappingStatus.EXPIRED
            PendingBackhaulMappingStatus.REVOKED -> {
                when (next) {
                    PendingBackhaulMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingBackhaulMappingStatus) {
        if (!canMoveTo(next)) {
            error("pending backhaul mapping $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((capacity.toDouble() - cost) * 100) / 100

    fun snapshot(): PendingBackhaulMapping {
        return PendingBackhaulMapping(
            code = code,
            name = name,
            updatedAt = updatedAt,
            priority = priority,
            cost = cost,
            pallets = pallets,
            capacity = capacity,
            fuelLiters = fuelLiters,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending backhaul mapping in status requested. */
fun newPendingBackhaulMapping(code: String, name: String): PendingBackhaulMapping {
    return PendingBackhaulMapping(
        code = code,
        name = name,
        updatedAt = 0,
        priority = 0,
        cost = 0.0,
        pallets = 0,
        capacity = 0,
        fuelLiters = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = PendingBackhaulMappingStatus.REQUESTED,
    )
}
