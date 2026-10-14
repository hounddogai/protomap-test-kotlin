package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered dwell milestone is in its life cycle. */
enum class TieredDwellMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered dwell milestone as the dwell team keeps it. */
data class TieredDwellMilestone(
    var code: String,
    var name: String,
    var country: String,
    var locale: String,
    var expiresAt: Int,
    var score: Double,
    var windowDays: Int,
    var status: TieredDwellMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDwellMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDwellMilestoneStatus): Boolean {
        when (status) {
            TieredDwellMilestoneStatus.OPEN -> {
                when (next) {
                    TieredDwellMilestoneStatus.RESOLVED -> return true
                }
                return false
            }
            TieredDwellMilestoneStatus.IN_REVIEW -> return next == TieredDwellMilestoneStatus.ARCHIVED
            TieredDwellMilestoneStatus.RESOLVED -> {
                when (next) {
                    TieredDwellMilestoneStatus.REOPENED, TieredDwellMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredDwellMilestoneStatus.REOPENED -> return next == TieredDwellMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredDwellMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tiered dwell milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((score - windowDays.toDouble()) * 100) / 100

    fun snapshot(): TieredDwellMilestone {
        return TieredDwellMilestone(
            code = code,
            name = name,
            country = country,
            locale = locale,
            expiresAt = expiresAt,
            score = score,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered dwell milestone in status open. */
fun newTieredDwellMilestone(code: String, name: String): TieredDwellMilestone {
    return TieredDwellMilestone(
        code = code,
        name = name,
        country = "",
        locale = "",
        expiresAt = 0,
        score = 0.0,
        windowDays = 0,
        status = TieredDwellMilestoneStatus.OPEN,
    )
}
