package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy coaching snapshot is in its life cycle. */
enum class LegacyCoachingSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy coaching snapshot. Its code is unique within a tenant. */
data class LegacyCoachingSnapshot(
    var code: String,
    var title: String,
    var budget: Double,
    var pallets: Int,
    var retries: Int,
    var trailerId: String,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var status: LegacyCoachingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCoachingSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCoachingSnapshotStatus): Boolean {
        when (status) {
            LegacyCoachingSnapshotStatus.IDLE -> {
                when (next) {
                    LegacyCoachingSnapshotStatus.WARMING, LegacyCoachingSnapshotStatus.READY -> return true
                }
                return false
            }
            LegacyCoachingSnapshotStatus.WARMING -> return next == LegacyCoachingSnapshotStatus.DRAINING || next == LegacyCoachingSnapshotStatus.STOPPED
            LegacyCoachingSnapshotStatus.READY -> {
                when (next) {
                    LegacyCoachingSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            LegacyCoachingSnapshotStatus.DRAINING -> {
                when (next) {
                    LegacyCoachingSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCoachingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy coaching snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(updatedAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): LegacyCoachingSnapshot {
        return LegacyCoachingSnapshot(
            code = code,
            title = title,
            budget = budget,
            pallets = pallets,
            retries = retries,
            trailerId = trailerId,
            updatedAt = updatedAt,
            attributes = attributes,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy coaching snapshot in status idle. */
fun newLegacyCoachingSnapshot(code: String, title: String): LegacyCoachingSnapshot {
    return LegacyCoachingSnapshot(
        code = code,
        title = title,
        budget = 0.0,
        pallets = 0,
        retries = 0,
        trailerId = "",
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        status = LegacyCoachingSnapshotStatus.IDLE,
    )
}
