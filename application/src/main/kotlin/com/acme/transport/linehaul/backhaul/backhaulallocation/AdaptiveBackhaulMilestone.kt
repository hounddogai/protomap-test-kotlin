package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive backhaul milestone is in its life cycle. */
enum class AdaptiveBackhaulMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive backhaul milestone and its current status. */
data class AdaptiveBackhaulMilestone(
    var code: String,
    var name: String,
    var threshold: Double,
    var score: Double,
    var unit: String,
    var retries: Int,
    var weight: Double,
    var revision: Int,
    var endsAt: Int,
    var priority: Int,
    var category: String,
    var status: AdaptiveBackhaulMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBackhaulMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBackhaulMilestoneStatus): Boolean {
        when (status) {
            AdaptiveBackhaulMilestoneStatus.IDLE -> {
                when (next) {
                    AdaptiveBackhaulMilestoneStatus.READY, AdaptiveBackhaulMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveBackhaulMilestoneStatus.WARMING -> return next == AdaptiveBackhaulMilestoneStatus.READY || next == AdaptiveBackhaulMilestoneStatus.DRAINING
            AdaptiveBackhaulMilestoneStatus.READY -> {
                when (next) {
                    AdaptiveBackhaulMilestoneStatus.DRAINING, AdaptiveBackhaulMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveBackhaulMilestoneStatus.DRAINING -> {
                when (next) {
                    AdaptiveBackhaulMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBackhaulMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive backhaul milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(retries.toDouble() * threshold * 100) / 100

    fun snapshot(): AdaptiveBackhaulMilestone {
        return AdaptiveBackhaulMilestone(
            code = code,
            name = name,
            threshold = threshold,
            score = score,
            unit = unit,
            retries = retries,
            weight = weight,
            revision = revision,
            endsAt = endsAt,
            priority = priority,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive backhaul milestone in status idle. */
fun newAdaptiveBackhaulMilestone(code: String, name: String): AdaptiveBackhaulMilestone {
    return AdaptiveBackhaulMilestone(
        code = code,
        name = name,
        threshold = 0.0,
        score = 0.0,
        unit = "",
        retries = 0,
        weight = 0.0,
        revision = 0,
        endsAt = 0,
        priority = 0,
        category = "",
        status = AdaptiveBackhaulMilestoneStatus.IDLE,
    )
}
