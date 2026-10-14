package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted defect snapshot is in its life cycle. */
enum class WeightedDefectSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted defect snapshot as the defect team keeps it. */
data class WeightedDefectSnapshot(
    var code: String,
    var label: String,
    var country: String,
    var checksum: String,
    var loadKg: Double,
    var name: String,
    var retries: Int,
    var status: WeightedDefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDefectSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDefectSnapshotStatus): Boolean {
        when (status) {
            WeightedDefectSnapshotStatus.CREATED -> {
                when (next) {
                    WeightedDefectSnapshotStatus.PUBLISHED, WeightedDefectSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            WeightedDefectSnapshotStatus.VALIDATED -> return next == WeightedDefectSnapshotStatus.RETIRED
            WeightedDefectSnapshotStatus.PUBLISHED -> return next == WeightedDefectSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedDefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted defect snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((retries.toDouble() - loadKg) * 100) / 100

    fun snapshot(): WeightedDefectSnapshot {
        return WeightedDefectSnapshot(
            code = code,
            label = label,
            country = country,
            checksum = checksum,
            loadKg = loadKg,
            name = name,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted defect snapshot in status created. */
fun newWeightedDefectSnapshot(code: String, label: String): WeightedDefectSnapshot {
    return WeightedDefectSnapshot(
        code = code,
        label = label,
        country = "",
        checksum = "",
        loadKg = 0.0,
        name = "",
        retries = 0,
        status = WeightedDefectSnapshotStatus.CREATED,
    )
}
