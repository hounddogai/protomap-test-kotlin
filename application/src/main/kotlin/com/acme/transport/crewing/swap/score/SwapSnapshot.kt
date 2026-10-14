package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.normalize

/** Where a swap snapshot is in its life cycle. */
enum class SwapSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A swap snapshot. Its code is unique within a tenant. */
data class SwapSnapshot(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var name: String,
    var country: String,
    var carrier: String,
    var amount: Double,
    var status: SwapSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapSnapshotStatus): Boolean {
        when (status) {
            SwapSnapshotStatus.PLANNED -> {
                when (next) {
                    SwapSnapshotStatus.SCHEDULED, SwapSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            SwapSnapshotStatus.SCHEDULED -> return next == SwapSnapshotStatus.IN_PROGRESS
            SwapSnapshotStatus.IN_PROGRESS -> return next == SwapSnapshotStatus.COMPLETED
            SwapSnapshotStatus.COMPLETED -> return next == SwapSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SwapSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("swap snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"swap snapshot"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = amount

    fun snapshot(): SwapSnapshot {
        return SwapSnapshot(
            code = code,
            title = title,
            tags = tags,
            name = name,
            country = country,
            carrier = carrier,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a swap snapshot in status planned. */
fun newSwapSnapshot(code: String, title: String): SwapSnapshot {
    return SwapSnapshot(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        name = "",
        country = "",
        carrier = "",
        amount = 0.0,
        status = SwapSnapshotStatus.PLANNED,
    )
}
