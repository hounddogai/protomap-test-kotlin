package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.text.normalize

/** Where a redelivery snapshot is in its life cycle. */
enum class RedeliverySnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A redelivery snapshot. Its code is unique within a tenant. */
data class RedeliverySnapshot(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var title: String,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var status: RedeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliverySnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliverySnapshotStatus): Boolean {
        when (status) {
            RedeliverySnapshotStatus.IDLE -> return next == RedeliverySnapshotStatus.WARMING
            RedeliverySnapshotStatus.WARMING -> return next == RedeliverySnapshotStatus.STOPPED
            RedeliverySnapshotStatus.READY -> {
                when (next) {
                    RedeliverySnapshotStatus.DRAINING, RedeliverySnapshotStatus.STOPPED -> return true
                }
                return false
            }
            RedeliverySnapshotStatus.DRAINING -> return next == RedeliverySnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RedeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("redelivery snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"redelivery snapshot"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = cost

    fun snapshot(): RedeliverySnapshot {
        return RedeliverySnapshot(
            code = code,
            label = label,
            tags = tags,
            title = title,
            cost = cost,
            attributes = attributes,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a redelivery snapshot in status idle. */
fun newRedeliverySnapshot(code: String, label: String): RedeliverySnapshot {
    return RedeliverySnapshot(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        title = "",
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        channel = "",
        status = RedeliverySnapshotStatus.IDLE,
    )
}
