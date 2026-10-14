package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback ev charge entry is in its life cycle. */
enum class FallbackEvChargeEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fallback ev charge entry, with what the planners need to act on it. */
data class FallbackEvChargeEntry(
    var code: String,
    var label: String,
    var ratio: Double,
    var limit: Int,
    var trailerId: String,
    var summary: String,
    var depot: String,
    var title: String,
    var status: FallbackEvChargeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeEntryStatus): Boolean {
        when (status) {
            FallbackEvChargeEntryStatus.REQUESTED -> return next == FallbackEvChargeEntryStatus.REVOKED
            FallbackEvChargeEntryStatus.GRANTED -> {
                when (next) {
                    FallbackEvChargeEntryStatus.REVOKED, FallbackEvChargeEntryStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackEvChargeEntryStatus.REVOKED -> return next == FallbackEvChargeEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeEntryStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round((ratio - limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackEvChargeEntry {
        return FallbackEvChargeEntry(
            code = code,
            label = label,
            ratio = ratio,
            limit = limit,
            trailerId = trailerId,
            summary = summary,
            depot = depot,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback ev charge entry in status requested. */
fun newFallbackEvChargeEntry(code: String, label: String): FallbackEvChargeEntry {
    return FallbackEvChargeEntry(
        code = code,
        label = label,
        ratio = 0.0,
        limit = 0,
        trailerId = "",
        summary = "",
        depot = "",
        title = "",
        status = FallbackEvChargeEntryStatus.REQUESTED,
    )
}
