package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled tender cycle is in its life cycle. */
enum class ScheduledTenderCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled tender cycle and its current status. */
data class ScheduledTenderCycle(
    var code: String,
    var title: String,
    var revision: Int,
    var driverId: String,
    var region: String,
    var active: Boolean,
    var createdAt: Int,
    var weight: Double,
    var status: ScheduledTenderCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTenderCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTenderCycleStatus): Boolean {
        when (status) {
            ScheduledTenderCycleStatus.OPEN -> {
                when (next) {
                    ScheduledTenderCycleStatus.IN_REVIEW, ScheduledTenderCycleStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledTenderCycleStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledTenderCycleStatus.RESOLVED, ScheduledTenderCycleStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledTenderCycleStatus.RESOLVED -> return next == ScheduledTenderCycleStatus.REOPENED
            ScheduledTenderCycleStatus.REOPENED -> return next == ScheduledTenderCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledTenderCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled tender cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round(createdAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): ScheduledTenderCycle {
        return ScheduledTenderCycle(
            code = code,
            title = title,
            revision = revision,
            driverId = driverId,
            region = region,
            active = active,
            createdAt = createdAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled tender cycle in status open. */
fun newScheduledTenderCycle(code: String, title: String): ScheduledTenderCycle {
    return ScheduledTenderCycle(
        code = code,
        title = title,
        revision = 0,
        driverId = "",
        region = "",
        active = false,
        createdAt = 0,
        weight = 0.0,
        status = ScheduledTenderCycleStatus.OPEN,
    )
}
