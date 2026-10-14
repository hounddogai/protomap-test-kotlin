package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge cycle is in its life cycle. */
enum class EvChargeCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A ev charge cycle. Its code is unique within a tenant. */
data class EvChargeCycle(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var unit: String,
    var timezone: String,
    var budget: Double,
    var dock: String,
    var floorValue: Double,
    var limit: Int,
    var durationMinutes: Int,
    var status: EvChargeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeCycleStatus): Boolean {
        when (status) {
            EvChargeCycleStatus.PLANNED -> return next == EvChargeCycleStatus.SCHEDULED || next == EvChargeCycleStatus.COMPLETED
            EvChargeCycleStatus.SCHEDULED -> {
                when (next) {
                    EvChargeCycleStatus.IN_PROGRESS, EvChargeCycleStatus.CANCELLED -> return true
                }
                return false
            }
            EvChargeCycleStatus.IN_PROGRESS -> {
                when (next) {
                    EvChargeCycleStatus.COMPLETED, EvChargeCycleStatus.CANCELLED -> return true
                }
                return false
            }
            EvChargeCycleStatus.COMPLETED -> {
                when (next) {
                    EvChargeCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeCycleStatus) {
        if (!canMoveTo(next)) {
            error("ev charge cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"ev charge cycle"} $code"

    fun netExposure(): Double = round((budget - limit.toDouble()) * 100) / 100

    fun snapshot(): EvChargeCycle {
        return EvChargeCycle(
            code = code,
            label = label,
            expiresAt = expiresAt,
            unit = unit,
            timezone = timezone,
            budget = budget,
            dock = dock,
            floorValue = floorValue,
            limit = limit,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a ev charge cycle in status planned. */
fun newEvChargeCycle(code: String, label: String): EvChargeCycle {
    return EvChargeCycle(
        code = code,
        label = label,
        expiresAt = 0,
        unit = "",
        timezone = "",
        budget = 0.0,
        dock = "",
        floorValue = 0.0,
        limit = 0,
        durationMinutes = 0,
        status = EvChargeCycleStatus.PLANNED,
    )
}
