package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default redelivery snapshot is in its life cycle. */
enum class DefaultRedeliverySnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default redelivery snapshot. Its code is unique within a tenant. */
data class DefaultRedeliverySnapshot(
    var code: String,
    var title: String,
    var approved: Boolean,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var distanceKm: Double,
    var label: String,
    var carrier: String,
    var status: DefaultRedeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRedeliverySnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRedeliverySnapshotStatus): Boolean {
        when (status) {
            DefaultRedeliverySnapshotStatus.IDLE -> {
                when (next) {
                    DefaultRedeliverySnapshotStatus.DRAINING -> return true
                }
                return false
            }
            DefaultRedeliverySnapshotStatus.WARMING -> {
                when (next) {
                    DefaultRedeliverySnapshotStatus.STOPPED -> return true
                }
                return false
            }
            DefaultRedeliverySnapshotStatus.READY -> return next == DefaultRedeliverySnapshotStatus.DRAINING || next == DefaultRedeliverySnapshotStatus.STOPPED
            DefaultRedeliverySnapshotStatus.DRAINING -> return next == DefaultRedeliverySnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultRedeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default redelivery snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default redelivery snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((startsAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): DefaultRedeliverySnapshot {
        return DefaultRedeliverySnapshot(
            code = code,
            title = title,
            approved = approved,
            depot = depot,
            attributes = attributes,
            startsAt = startsAt,
            distanceKm = distanceKm,
            label = label,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default redelivery snapshot in status idle. */
fun newDefaultRedeliverySnapshot(code: String, title: String): DefaultRedeliverySnapshot {
    return DefaultRedeliverySnapshot(
        code = code,
        title = title,
        approved = false,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        distanceKm = 0.0,
        label = "",
        carrier = "",
        status = DefaultRedeliverySnapshotStatus.IDLE,
    )
}
