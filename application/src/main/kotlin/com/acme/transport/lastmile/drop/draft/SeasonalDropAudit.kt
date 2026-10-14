package com.acme.transport.lastmile.drop.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal drop audit is in its life cycle. */
enum class SeasonalDropAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal drop audit and its current status. */
data class SeasonalDropAudit(
    var code: String,
    var label: String,
    var timezone: String,
    var threshold: Double,
    var score: Double,
    var graceDays: Int,
    var stopCount: Int,
    var maximum: Double,
    var margin: Double,
    var status: SeasonalDropAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDropAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDropAuditStatus): Boolean {
        when (status) {
            SeasonalDropAuditStatus.IDLE -> return next == SeasonalDropAuditStatus.WARMING || next == SeasonalDropAuditStatus.READY
            SeasonalDropAuditStatus.WARMING -> {
                when (next) {
                    SeasonalDropAuditStatus.READY -> return true
                }
                return false
            }
            SeasonalDropAuditStatus.READY -> {
                when (next) {
                    SeasonalDropAuditStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalDropAuditStatus.DRAINING -> {
                when (next) {
                    SeasonalDropAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDropAuditStatus) {
        if (!canMoveTo(next)) {
            error("seasonal drop audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal drop audit"} $code"

    fun weightedVolume(): Double = round((threshold - stopCount.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDropAudit {
        return SeasonalDropAudit(
            code = code,
            label = label,
            timezone = timezone,
            threshold = threshold,
            score = score,
            graceDays = graceDays,
            stopCount = stopCount,
            maximum = maximum,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal drop audit in status idle. */
fun newSeasonalDropAudit(code: String, label: String): SeasonalDropAudit {
    return SeasonalDropAudit(
        code = code,
        label = label,
        timezone = "",
        threshold = 0.0,
        score = 0.0,
        graceDays = 0,
        stopCount = 0,
        maximum = 0.0,
        margin = 0.0,
        status = SeasonalDropAuditStatus.IDLE,
    )
}
