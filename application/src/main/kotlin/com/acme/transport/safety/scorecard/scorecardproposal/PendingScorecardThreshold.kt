package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending scorecard threshold is in its life cycle. */
enum class PendingScorecardThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending scorecard threshold and its current status. */
data class PendingScorecardThreshold(
    var code: String,
    var title: String,
    var channel: String,
    var visible: Boolean,
    var label: String,
    var locale: String,
    var version: Int,
    var tags: MutableList<String>,
    var pallets: Int,
    var threshold: Double,
    var status: PendingScorecardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingScorecardThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingScorecardThresholdStatus): Boolean {
        when (status) {
            PendingScorecardThresholdStatus.REQUESTED -> return next == PendingScorecardThresholdStatus.GRANTED || next == PendingScorecardThresholdStatus.REVOKED
            PendingScorecardThresholdStatus.GRANTED -> {
                when (next) {
                    PendingScorecardThresholdStatus.REVOKED, PendingScorecardThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            PendingScorecardThresholdStatus.REVOKED -> return next == PendingScorecardThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingScorecardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending scorecard threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"pending scorecard threshold"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(pallets.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): PendingScorecardThreshold {
        return PendingScorecardThreshold(
            code = code,
            title = title,
            channel = channel,
            visible = visible,
            label = label,
            locale = locale,
            version = version,
            tags = tags,
            pallets = pallets,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending scorecard threshold in status requested. */
fun newPendingScorecardThreshold(code: String, title: String): PendingScorecardThreshold {
    return PendingScorecardThreshold(
        code = code,
        title = title,
        channel = "",
        visible = false,
        label = "",
        locale = "",
        version = 0,
        tags = mutableListOf<String>(),
        pallets = 0,
        threshold = 0.0,
        status = PendingScorecardThresholdStatus.REQUESTED,
    )
}
