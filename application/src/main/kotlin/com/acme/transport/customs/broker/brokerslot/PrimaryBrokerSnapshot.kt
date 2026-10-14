package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary broker snapshot is in its life cycle. */
enum class PrimaryBrokerSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary broker snapshot and its current status. */
data class PrimaryBrokerSnapshot(
    var code: String,
    var label: String,
    var segment: String,
    var timezone: String,
    var description: String,
    var priority: Int,
    var pallets: Int,
    var lane: String,
    var status: PrimaryBrokerSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBrokerSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBrokerSnapshotStatus): Boolean {
        when (status) {
            PrimaryBrokerSnapshotStatus.REQUESTED -> {
                when (next) {
                    PrimaryBrokerSnapshotStatus.GRANTED, PrimaryBrokerSnapshotStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryBrokerSnapshotStatus.GRANTED -> return next == PrimaryBrokerSnapshotStatus.EXPIRED
            PrimaryBrokerSnapshotStatus.REVOKED -> {
                when (next) {
                    PrimaryBrokerSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryBrokerSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary broker snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round(pallets.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): PrimaryBrokerSnapshot {
        return PrimaryBrokerSnapshot(
            code = code,
            label = label,
            segment = segment,
            timezone = timezone,
            description = description,
            priority = priority,
            pallets = pallets,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary broker snapshot in status requested. */
fun newPrimaryBrokerSnapshot(code: String, label: String): PrimaryBrokerSnapshot {
    return PrimaryBrokerSnapshot(
        code = code,
        label = label,
        segment = "",
        timezone = "",
        description = "",
        priority = 0,
        pallets = 0,
        lane = "",
        status = PrimaryBrokerSnapshotStatus.REQUESTED,
    )
}
