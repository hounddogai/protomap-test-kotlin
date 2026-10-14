package com.acme.transport.network.transittime.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted transit time snapshot is in its life cycle. */
enum class WeightedTransitTimeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a weighted transit time snapshot and its current status. */
data class WeightedTransitTimeSnapshot(
    var code: String,
    var label: String,
    var amount: Double,
    var routeCode: String,
    var trailerId: String,
    var attempts: Int,
    var ratio: Double,
    var status: WeightedTransitTimeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTransitTimeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTransitTimeSnapshotStatus): Boolean {
        when (status) {
            WeightedTransitTimeSnapshotStatus.CREATED -> {
                when (next) {
                    WeightedTransitTimeSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            WeightedTransitTimeSnapshotStatus.VALIDATED -> return next == WeightedTransitTimeSnapshotStatus.PUBLISHED
            WeightedTransitTimeSnapshotStatus.PUBLISHED -> return next == WeightedTransitTimeSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedTransitTimeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted transit time snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted transit time snapshot"} $code"

    fun effectiveValue(): Double = round(amount * attempts.toDouble() * 100) / 100

    fun snapshot(): WeightedTransitTimeSnapshot {
        return WeightedTransitTimeSnapshot(
            code = code,
            label = label,
            amount = amount,
            routeCode = routeCode,
            trailerId = trailerId,
            attempts = attempts,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted transit time snapshot in status created. */
fun newWeightedTransitTimeSnapshot(code: String, label: String): WeightedTransitTimeSnapshot {
    return WeightedTransitTimeSnapshot(
        code = code,
        label = label,
        amount = 0.0,
        routeCode = "",
        trailerId = "",
        attempts = 0,
        ratio = 0.0,
        status = WeightedTransitTimeSnapshotStatus.CREATED,
    )
}
