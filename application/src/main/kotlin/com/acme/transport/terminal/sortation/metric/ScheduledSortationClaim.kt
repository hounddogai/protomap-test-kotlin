package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled sortation claim is in its life cycle. */
enum class ScheduledSortationClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled sortation claim and its current status. */
data class ScheduledSortationClaim(
    var code: String,
    var label: String,
    var carrier: String,
    var trailerId: String,
    var ceiling: Double,
    var createdAt: Int,
    var lane: String,
    var stopCount: Int,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var status: ScheduledSortationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSortationClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSortationClaimStatus): Boolean {
        when (status) {
            ScheduledSortationClaimStatus.REQUESTED -> return next == ScheduledSortationClaimStatus.GRANTED
            ScheduledSortationClaimStatus.GRANTED -> return next == ScheduledSortationClaimStatus.REVOKED || next == ScheduledSortationClaimStatus.EXPIRED
            ScheduledSortationClaimStatus.REVOKED -> return next == ScheduledSortationClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledSortationClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled sortation claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((version.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledSortationClaim {
        return ScheduledSortationClaim(
            code = code,
            label = label,
            carrier = carrier,
            trailerId = trailerId,
            ceiling = ceiling,
            createdAt = createdAt,
            lane = lane,
            stopCount = stopCount,
            attributes = attributes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled sortation claim in status requested. */
fun newScheduledSortationClaim(code: String, label: String): ScheduledSortationClaim {
    return ScheduledSortationClaim(
        code = code,
        label = label,
        carrier = "",
        trailerId = "",
        ceiling = 0.0,
        createdAt = 0,
        lane = "",
        stopCount = 0,
        attributes = mutableMapOf<String, String>(),
        version = 0,
        status = ScheduledSortationClaimStatus.REQUESTED,
    )
}
