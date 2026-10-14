package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global defect snapshot is in its life cycle. */
enum class GlobalDefectSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global defect snapshot. Its code is unique within a tenant. */
data class GlobalDefectSnapshot(
    var code: String,
    var name: String,
    var shiftCode: String,
    var floorValue: Double,
    var dock: String,
    var updatedAt: Int,
    var loadKg: Double,
    var currency: String,
    var status: GlobalDefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDefectSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDefectSnapshotStatus): Boolean {
        when (status) {
            GlobalDefectSnapshotStatus.REQUESTED -> return next == GlobalDefectSnapshotStatus.EXPIRED
            GlobalDefectSnapshotStatus.GRANTED -> {
                when (next) {
                    GlobalDefectSnapshotStatus.REVOKED, GlobalDefectSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalDefectSnapshotStatus.REVOKED -> return next == GlobalDefectSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalDefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global defect snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((loadKg + floorValue) * 100) / 100

    fun snapshot(): GlobalDefectSnapshot {
        return GlobalDefectSnapshot(
            code = code,
            name = name,
            shiftCode = shiftCode,
            floorValue = floorValue,
            dock = dock,
            updatedAt = updatedAt,
            loadKg = loadKg,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global defect snapshot in status requested. */
fun newGlobalDefectSnapshot(code: String, name: String): GlobalDefectSnapshot {
    return GlobalDefectSnapshot(
        code = code,
        name = name,
        shiftCode = "",
        floorValue = 0.0,
        dock = "",
        updatedAt = 0,
        loadKg = 0.0,
        currency = "",
        status = GlobalDefectSnapshotStatus.REQUESTED,
    )
}
