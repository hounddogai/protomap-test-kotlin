package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive idling snapshot records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a adaptive idling snapshot is in its life cycle. */
enum class AdaptiveIdlingSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive idling snapshot as the idling team keeps it. */
data class AdaptiveIdlingSnapshot(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var limit: Int,
    var batchSize: Int,
    var rate: Double,
    var active: Boolean,
    var currency: String,
    var weight: Double,
    var status: AdaptiveIdlingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIdlingSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIdlingSnapshotStatus): Boolean {
        when (status) {
            AdaptiveIdlingSnapshotStatus.NEW -> return next == AdaptiveIdlingSnapshotStatus.QUEUED || next == AdaptiveIdlingSnapshotStatus.FAILED
            AdaptiveIdlingSnapshotStatus.QUEUED -> return next == AdaptiveIdlingSnapshotStatus.RUNNING || next == AdaptiveIdlingSnapshotStatus.DONE
            AdaptiveIdlingSnapshotStatus.RUNNING -> {
                when (next) {
                    AdaptiveIdlingSnapshotStatus.DONE, AdaptiveIdlingSnapshotStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveIdlingSnapshotStatus.DONE -> {
                when (next) {
                    AdaptiveIdlingSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveIdlingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive idling snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive idling snapshot"} $code"

    fun weightedValue(): Double = round((expiresAt.toDouble() + weight) * 100) / 100

    fun snapshot(): AdaptiveIdlingSnapshot {
        return AdaptiveIdlingSnapshot(
            code = code,
            title = title,
            expiresAt = expiresAt,
            limit = limit,
            batchSize = batchSize,
            rate = rate,
            active = active,
            currency = currency,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive idling snapshot in status new. */
fun newAdaptiveIdlingSnapshot(code: String, title: String): AdaptiveIdlingSnapshot {
    return AdaptiveIdlingSnapshot(
        code = code,
        title = title,
        expiresAt = 0,
        limit = 0,
        batchSize = 0,
        rate = 0.0,
        active = false,
        currency = "",
        weight = 0.0,
        status = AdaptiveIdlingSnapshotStatus.NEW,
    )
}
