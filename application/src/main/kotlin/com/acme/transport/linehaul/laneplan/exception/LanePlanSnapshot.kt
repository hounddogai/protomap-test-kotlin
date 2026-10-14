package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan snapshot is in its life cycle. */
enum class LanePlanSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lane plan snapshot. Its code is unique within a tenant. */
data class LanePlanSnapshot(
    var code: String,
    var title: String,
    var shiftCode: String,
    var updatedAt: Int,
    var depot: String,
    var minimum: Double,
    var revision: Int,
    var etaMinutes: Int,
    var segment: String,
    var createdAt: Int,
    var status: LanePlanSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanSnapshotStatus): Boolean {
        when (status) {
            LanePlanSnapshotStatus.DRAFT -> return next == LanePlanSnapshotStatus.PENDING || next == LanePlanSnapshotStatus.SUSPENDED
            LanePlanSnapshotStatus.PENDING -> {
                when (next) {
                    LanePlanSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            LanePlanSnapshotStatus.ACTIVE -> {
                when (next) {
                    LanePlanSnapshotStatus.SUSPENDED, LanePlanSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            LanePlanSnapshotStatus.SUSPENDED -> {
                when (next) {
                    LanePlanSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lane plan snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"lane plan snapshot"} $code"

    fun netTotal(): Double = round((updatedAt.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): LanePlanSnapshot {
        return LanePlanSnapshot(
            code = code,
            title = title,
            shiftCode = shiftCode,
            updatedAt = updatedAt,
            depot = depot,
            minimum = minimum,
            revision = revision,
            etaMinutes = etaMinutes,
            segment = segment,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lane plan snapshot in status draft. */
fun newLanePlanSnapshot(code: String, title: String): LanePlanSnapshot {
    return LanePlanSnapshot(
        code = code,
        title = title,
        shiftCode = "",
        updatedAt = 0,
        depot = "",
        minimum = 0.0,
        revision = 0,
        etaMinutes = 0,
        segment = "",
        createdAt = 0,
        status = LanePlanSnapshotStatus.DRAFT,
    )
}
