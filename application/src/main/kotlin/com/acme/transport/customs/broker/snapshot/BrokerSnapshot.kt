package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker snapshot records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker snapshot is in its life cycle. */
enum class BrokerSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A broker snapshot as the broker team keeps it. */
data class BrokerSnapshot(
    var code: String,
    var label: String,
    var timezone: String,
    var ratio: Double,
    var tags: MutableList<String>,
    var pinned: Boolean,
    var routeCode: String,
    var maximum: Double,
    var source: String,
    var status: BrokerSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerSnapshotStatus): Boolean {
        when (status) {
            BrokerSnapshotStatus.IDLE -> return next == BrokerSnapshotStatus.WARMING
            BrokerSnapshotStatus.WARMING -> return next == BrokerSnapshotStatus.DRAINING
            BrokerSnapshotStatus.READY -> return next == BrokerSnapshotStatus.STOPPED
            BrokerSnapshotStatus.DRAINING -> {
                when (next) {
                    BrokerSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("broker snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((ratio + maximum) * 100) / 100

    fun snapshot(): BrokerSnapshot {
        return BrokerSnapshot(
            code = code,
            label = label,
            timezone = timezone,
            ratio = ratio,
            tags = tags,
            pinned = pinned,
            routeCode = routeCode,
            maximum = maximum,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a broker snapshot in status idle. */
fun newBrokerSnapshot(code: String, label: String): BrokerSnapshot {
    return BrokerSnapshot(
        code = code,
        label = label,
        timezone = "",
        ratio = 0.0,
        tags = mutableListOf<String>(),
        pinned = false,
        routeCode = "",
        maximum = 0.0,
        source = "",
        status = BrokerSnapshotStatus.IDLE,
    )
}
