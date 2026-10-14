package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy utilization baseline is in its life cycle. */
enum class LegacyUtilizationBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy utilization baseline, with what the planners need to act on it. */
data class LegacyUtilizationBaseline(
    var code: String,
    var title: String,
    var lane: String,
    var reviewer: String,
    var ceiling: Double,
    var margin: Double,
    var pinned: Boolean,
    var contactEmail: String,
    var maximum: Double,
    var status: LegacyUtilizationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyUtilizationBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyUtilizationBaselineStatus): Boolean {
        when (status) {
            LegacyUtilizationBaselineStatus.NEW -> return next == LegacyUtilizationBaselineStatus.QUEUED
            LegacyUtilizationBaselineStatus.QUEUED -> return next == LegacyUtilizationBaselineStatus.DONE || next == LegacyUtilizationBaselineStatus.FAILED
            LegacyUtilizationBaselineStatus.RUNNING -> return next == LegacyUtilizationBaselineStatus.FAILED
            LegacyUtilizationBaselineStatus.DONE -> return next == LegacyUtilizationBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyUtilizationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("legacy utilization baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round(margin * maximum * 100) / 100

    fun snapshot(): LegacyUtilizationBaseline {
        return LegacyUtilizationBaseline(
            code = code,
            title = title,
            lane = lane,
            reviewer = reviewer,
            ceiling = ceiling,
            margin = margin,
            pinned = pinned,
            contactEmail = contactEmail,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy utilization baseline in status new. */
fun newLegacyUtilizationBaseline(code: String, title: String): LegacyUtilizationBaseline {
    return LegacyUtilizationBaseline(
        code = code,
        title = title,
        lane = "",
        reviewer = "",
        ceiling = 0.0,
        margin = 0.0,
        pinned = false,
        contactEmail = "",
        maximum = 0.0,
        status = LegacyUtilizationBaselineStatus.NEW,
    )
}
