package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary node milestone is in its life cycle. */
enum class PrimaryNodeMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary node milestone as the node team keeps it. */
data class PrimaryNodeMilestone(
    var code: String,
    var name: String,
    var budget: Double,
    var floorValue: Double,
    var weight: Double,
    var windowDays: Int,
    var odometer: Int,
    var locale: String,
    var version: Int,
    var status: PrimaryNodeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNodeMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNodeMilestoneStatus): Boolean {
        when (status) {
            PrimaryNodeMilestoneStatus.DRAFT -> {
                when (next) {
                    PrimaryNodeMilestoneStatus.ACTIVE, PrimaryNodeMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryNodeMilestoneStatus.PENDING -> {
                when (next) {
                    PrimaryNodeMilestoneStatus.ACTIVE, PrimaryNodeMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryNodeMilestoneStatus.ACTIVE -> return next == PrimaryNodeMilestoneStatus.SUSPENDED || next == PrimaryNodeMilestoneStatus.CLOSED
            PrimaryNodeMilestoneStatus.SUSPENDED -> {
                when (next) {
                    PrimaryNodeMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNodeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("primary node milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((odometer.toDouble() - weight) * 100) / 100

    fun snapshot(): PrimaryNodeMilestone {
        return PrimaryNodeMilestone(
            code = code,
            name = name,
            budget = budget,
            floorValue = floorValue,
            weight = weight,
            windowDays = windowDays,
            odometer = odometer,
            locale = locale,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary node milestone in status draft. */
fun newPrimaryNodeMilestone(code: String, name: String): PrimaryNodeMilestone {
    return PrimaryNodeMilestone(
        code = code,
        name = name,
        budget = 0.0,
        floorValue = 0.0,
        weight = 0.0,
        windowDays = 0,
        odometer = 0,
        locale = "",
        version = 0,
        status = PrimaryNodeMilestoneStatus.DRAFT,
    )
}
