package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional utilization cycle is in its life cycle. */
enum class RegionalUtilizationCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional utilization cycle and its current status. */
data class RegionalUtilizationCycle(
    var code: String,
    var title: String,
    var amount: Double,
    var active: Boolean,
    var notes: String,
    var priority: Int,
    var discount: Double,
    var attributes: MutableMap<String, String>,
    var revision: Int,
    var status: RegionalUtilizationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalUtilizationCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalUtilizationCycleStatus): Boolean {
        when (status) {
            RegionalUtilizationCycleStatus.REQUESTED -> return next == RegionalUtilizationCycleStatus.GRANTED
            RegionalUtilizationCycleStatus.GRANTED -> return next == RegionalUtilizationCycleStatus.EXPIRED
            RegionalUtilizationCycleStatus.REVOKED -> {
                when (next) {
                    RegionalUtilizationCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalUtilizationCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional utilization cycle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"regional utilization cycle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((revision.toDouble() - amount) * 100) / 100

    fun snapshot(): RegionalUtilizationCycle {
        return RegionalUtilizationCycle(
            code = code,
            title = title,
            amount = amount,
            active = active,
            notes = notes,
            priority = priority,
            discount = discount,
            attributes = attributes,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional utilization cycle in status requested. */
fun newRegionalUtilizationCycle(code: String, title: String): RegionalUtilizationCycle {
    return RegionalUtilizationCycle(
        code = code,
        title = title,
        amount = 0.0,
        active = false,
        notes = "",
        priority = 0,
        discount = 0.0,
        attributes = mutableMapOf<String, String>(),
        revision = 0,
        status = RegionalUtilizationCycleStatus.REQUESTED,
    )
}
