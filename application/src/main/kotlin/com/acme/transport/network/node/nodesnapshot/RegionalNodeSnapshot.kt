package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional node snapshot records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a regional node snapshot is in its life cycle. */
enum class RegionalNodeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional node snapshot and its current status. */
data class RegionalNodeSnapshot(
    var code: String,
    var name: String,
    var startsAt: Int,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var carrier: String,
    var sequence: Int,
    var attempts: Int,
    var phone: String,
    var budget: Double,
    var priority: Int,
    var status: RegionalNodeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNodeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNodeSnapshotStatus): Boolean {
        when (status) {
            RegionalNodeSnapshotStatus.CREATED -> return next == RegionalNodeSnapshotStatus.RETIRED
            RegionalNodeSnapshotStatus.VALIDATED -> return next == RegionalNodeSnapshotStatus.RETIRED
            RegionalNodeSnapshotStatus.PUBLISHED -> return next == RegionalNodeSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalNodeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional node snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional node snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((priority.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalNodeSnapshot {
        return RegionalNodeSnapshot(
            code = code,
            name = name,
            startsAt = startsAt,
            amount = amount,
            attributes = attributes,
            carrier = carrier,
            sequence = sequence,
            attempts = attempts,
            phone = phone,
            budget = budget,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional node snapshot in status created. */
fun newRegionalNodeSnapshot(code: String, name: String): RegionalNodeSnapshot {
    return RegionalNodeSnapshot(
        code = code,
        name = name,
        startsAt = 0,
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        carrier = "",
        sequence = 0,
        attempts = 0,
        phone = "",
        budget = 0.0,
        priority = 0,
        status = RegionalNodeSnapshotStatus.CREATED,
    )
}
