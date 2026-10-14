package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization cycle is in its life cycle. */
enum class UtilizationCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a utilization cycle and its current status. */
data class UtilizationCycle(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var pinned: Boolean,
    var title: String,
    var externalRef: String,
    var tags: MutableList<String>,
    var priority: Int,
    var status: UtilizationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationCycleStatus): Boolean {
        when (status) {
            UtilizationCycleStatus.REQUESTED -> return next == UtilizationCycleStatus.GRANTED || next == UtilizationCycleStatus.EXPIRED
            UtilizationCycleStatus.GRANTED -> {
                when (next) {
                    UtilizationCycleStatus.EXPIRED -> return true
                }
                return false
            }
            UtilizationCycleStatus.REVOKED -> {
                when (next) {
                    UtilizationCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationCycleStatus) {
        if (!canMoveTo(next)) {
            error("utilization cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"utilization cycle"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((fuelLiters - priority.toDouble()) * 100) / 100

    fun snapshot(): UtilizationCycle {
        return UtilizationCycle(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            pinned = pinned,
            title = title,
            externalRef = externalRef,
            tags = tags,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization cycle in status requested. */
fun newUtilizationCycle(code: String, label: String): UtilizationCycle {
    return UtilizationCycle(
        code = code,
        label = label,
        fuelLiters = 0.0,
        pinned = false,
        title = "",
        externalRef = "",
        tags = mutableListOf<String>(),
        priority = 0,
        status = UtilizationCycleStatus.REQUESTED,
    )
}
