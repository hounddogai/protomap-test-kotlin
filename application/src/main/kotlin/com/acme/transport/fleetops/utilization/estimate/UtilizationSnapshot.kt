package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization snapshot is in its life cycle. */
enum class UtilizationSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A utilization snapshot. Its code is unique within a tenant. */
data class UtilizationSnapshot(
    var code: String,
    var title: String,
    var revision: Int,
    var lane: String,
    var loadKg: Double,
    var limit: Int,
    var phone: String,
    var reviewer: String,
    var source: String,
    var description: String,
    var unit: String,
    var status: UtilizationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationSnapshotStatus): Boolean {
        when (status) {
            UtilizationSnapshotStatus.OPEN -> {
                when (next) {
                    UtilizationSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            UtilizationSnapshotStatus.IN_REVIEW -> return next == UtilizationSnapshotStatus.RESOLVED || next == UtilizationSnapshotStatus.ARCHIVED
            UtilizationSnapshotStatus.RESOLVED -> {
                when (next) {
                    UtilizationSnapshotStatus.REOPENED, UtilizationSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            UtilizationSnapshotStatus.REOPENED -> {
                when (next) {
                    UtilizationSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("utilization snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"utilization snapshot"} $code"

    fun projectedExposure(): Double = round((revision.toDouble() - loadKg) * 100) / 100

    fun snapshot(): UtilizationSnapshot {
        return UtilizationSnapshot(
            code = code,
            title = title,
            revision = revision,
            lane = lane,
            loadKg = loadKg,
            limit = limit,
            phone = phone,
            reviewer = reviewer,
            source = source,
            description = description,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a utilization snapshot in status open. */
fun newUtilizationSnapshot(code: String, title: String): UtilizationSnapshot {
    return UtilizationSnapshot(
        code = code,
        title = title,
        revision = 0,
        lane = "",
        loadKg = 0.0,
        limit = 0,
        phone = "",
        reviewer = "",
        source = "",
        description = "",
        unit = "",
        status = UtilizationSnapshotStatus.OPEN,
    )
}
