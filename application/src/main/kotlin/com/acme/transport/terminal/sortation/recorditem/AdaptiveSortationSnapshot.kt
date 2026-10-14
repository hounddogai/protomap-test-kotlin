package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive sortation snapshot is in its life cycle. */
enum class AdaptiveSortationSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive sortation snapshot. Its code is unique within a tenant. */
data class AdaptiveSortationSnapshot(
    var code: String,
    var name: String,
    var version: Int,
    var budget: Double,
    var contactEmail: String,
    var batchSize: Int,
    var stopCount: Int,
    var depot: String,
    var source: String,
    var minimum: Double,
    var status: AdaptiveSortationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSortationSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSortationSnapshotStatus): Boolean {
        when (status) {
            AdaptiveSortationSnapshotStatus.NEW -> {
                when (next) {
                    AdaptiveSortationSnapshotStatus.DONE, AdaptiveSortationSnapshotStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveSortationSnapshotStatus.QUEUED -> return next == AdaptiveSortationSnapshotStatus.DONE || next == AdaptiveSortationSnapshotStatus.FAILED
            AdaptiveSortationSnapshotStatus.RUNNING -> return next == AdaptiveSortationSnapshotStatus.FAILED
            AdaptiveSortationSnapshotStatus.DONE -> {
                when (next) {
                    AdaptiveSortationSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveSortationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive sortation snapshot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(version.toDouble() * minimum * 100) / 100

    fun snapshot(): AdaptiveSortationSnapshot {
        return AdaptiveSortationSnapshot(
            code = code,
            name = name,
            version = version,
            budget = budget,
            contactEmail = contactEmail,
            batchSize = batchSize,
            stopCount = stopCount,
            depot = depot,
            source = source,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive sortation snapshot in status new. */
fun newAdaptiveSortationSnapshot(code: String, name: String): AdaptiveSortationSnapshot {
    return AdaptiveSortationSnapshot(
        code = code,
        name = name,
        version = 0,
        budget = 0.0,
        contactEmail = "",
        batchSize = 0,
        stopCount = 0,
        depot = "",
        source = "",
        minimum = 0.0,
        status = AdaptiveSortationSnapshotStatus.NEW,
    )
}
