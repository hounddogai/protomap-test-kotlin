package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.text.normalize

// The lenient node snapshot records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a lenient node snapshot is in its life cycle. */
enum class LenientNodeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient node snapshot as the node team keeps it. */
data class LenientNodeSnapshot(
    var code: String,
    var name: String,
    var carrier: String,
    var tags: MutableList<String>,
    var retries: Int,
    var segment: String,
    var status: LenientNodeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNodeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNodeSnapshotStatus): Boolean {
        when (status) {
            LenientNodeSnapshotStatus.CREATED -> {
                when (next) {
                    LenientNodeSnapshotStatus.VALIDATED, LenientNodeSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientNodeSnapshotStatus.VALIDATED -> return next == LenientNodeSnapshotStatus.RETIRED
            LenientNodeSnapshotStatus.PUBLISHED -> {
                when (next) {
                    LenientNodeSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientNodeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lenient node snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): LenientNodeSnapshot {
        return LenientNodeSnapshot(
            code = code,
            name = name,
            carrier = carrier,
            tags = tags,
            retries = retries,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient node snapshot in status created. */
fun newLenientNodeSnapshot(code: String, name: String): LenientNodeSnapshot {
    return LenientNodeSnapshot(
        code = code,
        name = name,
        carrier = "",
        tags = mutableListOf<String>(),
        retries = 0,
        segment = "",
        status = LenientNodeSnapshotStatus.CREATED,
    )
}
