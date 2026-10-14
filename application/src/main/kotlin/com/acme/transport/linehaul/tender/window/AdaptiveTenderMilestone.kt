package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive tender milestone is in its life cycle. */
enum class AdaptiveTenderMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a adaptive tender milestone and its current status. */
data class AdaptiveTenderMilestone(
    var code: String,
    var title: String,
    var lane: String,
    var amount: Double,
    var cost: Double,
    var approved: Boolean,
    var rate: Double,
    var description: String,
    var weight: Double,
    var ratio: Double,
    var status: AdaptiveTenderMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTenderMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTenderMilestoneStatus): Boolean {
        when (status) {
            AdaptiveTenderMilestoneStatus.NEW -> {
                when (next) {
                    AdaptiveTenderMilestoneStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveTenderMilestoneStatus.QUEUED -> {
                when (next) {
                    AdaptiveTenderMilestoneStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveTenderMilestoneStatus.RUNNING -> {
                when (next) {
                    AdaptiveTenderMilestoneStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveTenderMilestoneStatus.DONE -> return next == AdaptiveTenderMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveTenderMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tender milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive tender milestone"} $code"

    fun weightedLoad(): Double = round((rate + ratio) * 100) / 100

    fun snapshot(): AdaptiveTenderMilestone {
        return AdaptiveTenderMilestone(
            code = code,
            title = title,
            lane = lane,
            amount = amount,
            cost = cost,
            approved = approved,
            rate = rate,
            description = description,
            weight = weight,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive tender milestone in status new. */
fun newAdaptiveTenderMilestone(code: String, title: String): AdaptiveTenderMilestone {
    return AdaptiveTenderMilestone(
        code = code,
        title = title,
        lane = "",
        amount = 0.0,
        cost = 0.0,
        approved = false,
        rate = 0.0,
        description = "",
        weight = 0.0,
        ratio = 0.0,
        status = AdaptiveTenderMilestoneStatus.NEW,
    )
}
