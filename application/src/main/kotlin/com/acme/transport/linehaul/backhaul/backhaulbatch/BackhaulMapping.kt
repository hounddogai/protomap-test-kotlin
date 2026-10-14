package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul mapping is in its life cycle. */
enum class BackhaulMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a backhaul mapping and its current status. */
data class BackhaulMapping(
    var code: String,
    var label: String,
    var capacity: Int,
    var notes: String,
    var trailerId: String,
    var weight: Double,
    var unit: String,
    var status: BackhaulMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulMappingStatus): Boolean {
        when (status) {
            BackhaulMappingStatus.IDLE -> return next == BackhaulMappingStatus.WARMING
            BackhaulMappingStatus.WARMING -> {
                when (next) {
                    BackhaulMappingStatus.READY -> return true
                }
                return false
            }
            BackhaulMappingStatus.READY -> {
                when (next) {
                    BackhaulMappingStatus.DRAINING -> return true
                }
                return false
            }
            BackhaulMappingStatus.DRAINING -> return next == BackhaulMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BackhaulMappingStatus) {
        if (!canMoveTo(next)) {
            error("backhaul mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round((capacity.toDouble() - weight) * 100) / 100

    fun snapshot(): BackhaulMapping {
        return BackhaulMapping(
            code = code,
            label = label,
            capacity = capacity,
            notes = notes,
            trailerId = trailerId,
            weight = weight,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a backhaul mapping in status idle. */
fun newBackhaulMapping(code: String, label: String): BackhaulMapping {
    return BackhaulMapping(
        code = code,
        label = label,
        capacity = 0,
        notes = "",
        trailerId = "",
        weight = 0.0,
        unit = "",
        status = BackhaulMappingStatus.IDLE,
    )
}
