package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict node bundle records of network's node work, and the rules, stores, and calculations around them.

/** Where a strict node bundle is in its life cycle. */
enum class StrictNodeBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict node bundle as the node team keeps it. */
data class StrictNodeBundle(
    var code: String,
    var label: String,
    var reviewer: String,
    var segment: String,
    var startsAt: Int,
    var sequence: Int,
    var region: String,
    var limit: Int,
    var rate: Double,
    var revision: Int,
    var status: StrictNodeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNodeBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNodeBundleStatus): Boolean {
        when (status) {
            StrictNodeBundleStatus.DRAFT -> return next == StrictNodeBundleStatus.CLOSED
            StrictNodeBundleStatus.PENDING -> {
                when (next) {
                    StrictNodeBundleStatus.ACTIVE, StrictNodeBundleStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictNodeBundleStatus.ACTIVE -> return next == StrictNodeBundleStatus.CLOSED
            StrictNodeBundleStatus.SUSPENDED -> return next == StrictNodeBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictNodeBundleStatus) {
        if (!canMoveTo(next)) {
            error("strict node bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((revision.toDouble() + rate) * 100) / 100

    fun snapshot(): StrictNodeBundle {
        return StrictNodeBundle(
            code = code,
            label = label,
            reviewer = reviewer,
            segment = segment,
            startsAt = startsAt,
            sequence = sequence,
            region = region,
            limit = limit,
            rate = rate,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict node bundle in status draft. */
fun newStrictNodeBundle(code: String, label: String): StrictNodeBundle {
    return StrictNodeBundle(
        code = code,
        label = label,
        reviewer = "",
        segment = "",
        startsAt = 0,
        sequence = 0,
        region = "",
        limit = 0,
        rate = 0.0,
        revision = 0,
        status = StrictNodeBundleStatus.DRAFT,
    )
}
