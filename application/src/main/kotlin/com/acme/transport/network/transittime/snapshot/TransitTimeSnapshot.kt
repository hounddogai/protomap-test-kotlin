package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time snapshot records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time snapshot is in its life cycle. */
enum class TransitTimeSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A transit time snapshot as the transit time team keeps it. */
data class TransitTimeSnapshot(
    var code: String,
    var title: String,
    var maximum: Double,
    var depot: String,
    var revision: Int,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var createdAt: Int,
    var unit: String,
    var weight: Double,
    var status: TransitTimeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeSnapshotStatus): Boolean {
        when (status) {
            TransitTimeSnapshotStatus.PLANNED -> {
                when (next) {
                    TransitTimeSnapshotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TransitTimeSnapshotStatus.SCHEDULED -> {
                when (next) {
                    TransitTimeSnapshotStatus.IN_PROGRESS, TransitTimeSnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            TransitTimeSnapshotStatus.IN_PROGRESS -> return next == TransitTimeSnapshotStatus.COMPLETED
            TransitTimeSnapshotStatus.COMPLETED -> {
                when (next) {
                    TransitTimeSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("transit time snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round(maximum * weight * 100) / 100

    fun snapshot(): TransitTimeSnapshot {
        return TransitTimeSnapshot(
            code = code,
            title = title,
            maximum = maximum,
            depot = depot,
            revision = revision,
            attributes = attributes,
            summary = summary,
            createdAt = createdAt,
            unit = unit,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a transit time snapshot in status planned. */
fun newTransitTimeSnapshot(code: String, title: String): TransitTimeSnapshot {
    return TransitTimeSnapshot(
        code = code,
        title = title,
        maximum = 0.0,
        depot = "",
        revision = 0,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        createdAt = 0,
        unit = "",
        weight = 0.0,
        status = TransitTimeSnapshotStatus.PLANNED,
    )
}
