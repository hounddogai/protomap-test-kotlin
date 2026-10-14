package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional utilization segment records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a regional utilization segment is in its life cycle. */
enum class RegionalUtilizationSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional utilization segment and its current status. */
data class RegionalUtilizationSegment(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var expiresAt: Int,
    var pallets: Int,
    var cost: Double,
    var timezone: String,
    var stopCount: Int,
    var status: RegionalUtilizationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalUtilizationSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalUtilizationSegmentStatus): Boolean {
        when (status) {
            RegionalUtilizationSegmentStatus.NEW -> return next == RegionalUtilizationSegmentStatus.RUNNING
            RegionalUtilizationSegmentStatus.QUEUED -> return next == RegionalUtilizationSegmentStatus.RUNNING || next == RegionalUtilizationSegmentStatus.FAILED
            RegionalUtilizationSegmentStatus.RUNNING -> {
                when (next) {
                    RegionalUtilizationSegmentStatus.DONE, RegionalUtilizationSegmentStatus.FAILED -> return true
                }
                return false
            }
            RegionalUtilizationSegmentStatus.DONE -> {
                when (next) {
                    RegionalUtilizationSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalUtilizationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("regional utilization segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((cost - pallets.toDouble()) * 100) / 100

    fun snapshot(): RegionalUtilizationSegment {
        return RegionalUtilizationSegment(
            code = code,
            label = label,
            tags = tags,
            expiresAt = expiresAt,
            pallets = pallets,
            cost = cost,
            timezone = timezone,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional utilization segment in status new. */
fun newRegionalUtilizationSegment(code: String, label: String): RegionalUtilizationSegment {
    return RegionalUtilizationSegment(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        expiresAt = 0,
        pallets = 0,
        cost = 0.0,
        timezone = "",
        stopCount = 0,
        status = RegionalUtilizationSegmentStatus.NEW,
    )
}
