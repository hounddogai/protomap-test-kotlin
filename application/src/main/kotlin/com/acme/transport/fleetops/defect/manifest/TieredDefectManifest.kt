package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered defect manifest records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a tiered defect manifest is in its life cycle. */
enum class TieredDefectManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered defect manifest as the defect team keeps it. */
data class TieredDefectManifest(
    var code: String,
    var title: String,
    var limit: Int,
    var segment: String,
    var locale: String,
    var loadKg: Double,
    var status: TieredDefectManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDefectManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDefectManifestStatus): Boolean {
        when (status) {
            TieredDefectManifestStatus.CREATED -> {
                when (next) {
                    TieredDefectManifestStatus.PUBLISHED, TieredDefectManifestStatus.RETIRED -> return true
                }
                return false
            }
            TieredDefectManifestStatus.VALIDATED -> {
                when (next) {
                    TieredDefectManifestStatus.PUBLISHED, TieredDefectManifestStatus.RETIRED -> return true
                }
                return false
            }
            TieredDefectManifestStatus.PUBLISHED -> {
                when (next) {
                    TieredDefectManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDefectManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered defect manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((limit.toDouble() + loadKg) * 100) / 100

    fun snapshot(): TieredDefectManifest {
        return TieredDefectManifest(
            code = code,
            title = title,
            limit = limit,
            segment = segment,
            locale = locale,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered defect manifest in status created. */
fun newTieredDefectManifest(code: String, title: String): TieredDefectManifest {
    return TieredDefectManifest(
        code = code,
        title = title,
        limit = 0,
        segment = "",
        locale = "",
        loadKg = 0.0,
        status = TieredDefectManifestStatus.CREATED,
    )
}
