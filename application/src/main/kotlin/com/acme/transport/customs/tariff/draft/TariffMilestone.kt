package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff milestone is in its life cycle. */
enum class TariffMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tariff milestone and its current status. */
data class TariffMilestone(
    var code: String,
    var name: String,
    var createdAt: Int,
    var updatedAt: Int,
    var ceiling: Double,
    var cost: Double,
    var externalRef: String,
    var ownerId: String,
    var stopCount: Int,
    var summary: String,
    var status: TariffMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffMilestoneStatus): Boolean {
        when (status) {
            TariffMilestoneStatus.IDLE -> return next == TariffMilestoneStatus.READY || next == TariffMilestoneStatus.DRAINING
            TariffMilestoneStatus.WARMING -> return next == TariffMilestoneStatus.STOPPED
            TariffMilestoneStatus.READY -> {
                when (next) {
                    TariffMilestoneStatus.DRAINING, TariffMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            TariffMilestoneStatus.DRAINING -> {
                when (next) {
                    TariffMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tariff milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round(createdAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): TariffMilestone {
        return TariffMilestone(
            code = code,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ceiling = ceiling,
            cost = cost,
            externalRef = externalRef,
            ownerId = ownerId,
            stopCount = stopCount,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tariff milestone in status idle. */
fun newTariffMilestone(code: String, name: String): TariffMilestone {
    return TariffMilestone(
        code = code,
        name = name,
        createdAt = 0,
        updatedAt = 0,
        ceiling = 0.0,
        cost = 0.0,
        externalRef = "",
        ownerId = "",
        stopCount = 0,
        summary = "",
        status = TariffMilestoneStatus.IDLE,
    )
}
