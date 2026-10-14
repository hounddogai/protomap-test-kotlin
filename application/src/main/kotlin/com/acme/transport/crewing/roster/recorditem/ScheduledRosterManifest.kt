package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled roster manifest is in its life cycle. */
enum class ScheduledRosterManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled roster manifest as the roster team keeps it. */
data class ScheduledRosterManifest(
    var code: String,
    var label: String,
    var margin: Double,
    var batchSize: Int,
    var title: String,
    var active: Boolean,
    var attempts: Int,
    var priority: Int,
    var channel: String,
    var status: ScheduledRosterManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterManifestStatus): Boolean {
        when (status) {
            ScheduledRosterManifestStatus.PROPOSED -> return next == ScheduledRosterManifestStatus.APPROVED || next == ScheduledRosterManifestStatus.WITHDRAWN
            ScheduledRosterManifestStatus.APPROVED -> return next == ScheduledRosterManifestStatus.WITHDRAWN
            ScheduledRosterManifestStatus.REJECTED -> {
                when (next) {
                    ScheduledRosterManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRosterManifestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled roster manifest"} $code"

    fun netLoad(): Double = round((priority.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRosterManifest {
        return ScheduledRosterManifest(
            code = code,
            label = label,
            margin = margin,
            batchSize = batchSize,
            title = title,
            active = active,
            attempts = attempts,
            priority = priority,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled roster manifest in status proposed. */
fun newScheduledRosterManifest(code: String, label: String): ScheduledRosterManifest {
    return ScheduledRosterManifest(
        code = code,
        label = label,
        margin = 0.0,
        batchSize = 0,
        title = "",
        active = false,
        attempts = 0,
        priority = 0,
        channel = "",
        status = ScheduledRosterManifestStatus.PROPOSED,
    )
}
