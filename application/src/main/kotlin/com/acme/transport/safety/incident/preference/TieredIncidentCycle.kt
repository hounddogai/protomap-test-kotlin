package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered incident cycle is in its life cycle. */
enum class TieredIncidentCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered incident cycle as the incident team keeps it. */
data class TieredIncidentCycle(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var batchSize: Int,
    var externalRef: String,
    var quantity: Int,
    var visible: Boolean,
    var minimum: Double,
    var lane: String,
    var timezone: String,
    var status: TieredIncidentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIncidentCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIncidentCycleStatus): Boolean {
        when (status) {
            TieredIncidentCycleStatus.OPEN -> return next == TieredIncidentCycleStatus.REOPENED
            TieredIncidentCycleStatus.IN_REVIEW -> {
                when (next) {
                    TieredIncidentCycleStatus.REOPENED -> return true
                }
                return false
            }
            TieredIncidentCycleStatus.RESOLVED -> {
                when (next) {
                    TieredIncidentCycleStatus.REOPENED, TieredIncidentCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredIncidentCycleStatus.REOPENED -> {
                when (next) {
                    TieredIncidentCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIncidentCycleStatus) {
        if (!canMoveTo(next)) {
            error("tiered incident cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered incident cycle"} $code"

    fun adjustedLoad(): Double = round((minimum + batchSize.toDouble()) * 100) / 100

    fun snapshot(): TieredIncidentCycle {
        return TieredIncidentCycle(
            code = code,
            name = name,
            pinned = pinned,
            batchSize = batchSize,
            externalRef = externalRef,
            quantity = quantity,
            visible = visible,
            minimum = minimum,
            lane = lane,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered incident cycle in status open. */
fun newTieredIncidentCycle(code: String, name: String): TieredIncidentCycle {
    return TieredIncidentCycle(
        code = code,
        name = name,
        pinned = false,
        batchSize = 0,
        externalRef = "",
        quantity = 0,
        visible = false,
        minimum = 0.0,
        lane = "",
        timezone = "",
        status = TieredIncidentCycleStatus.OPEN,
    )
}
