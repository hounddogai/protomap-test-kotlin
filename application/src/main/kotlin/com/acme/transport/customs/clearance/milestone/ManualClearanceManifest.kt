package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual clearance manifest is in its life cycle. */
enum class ManualClearanceManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual clearance manifest. Its code is unique within a tenant. */
data class ManualClearanceManifest(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var depot: String,
    var shiftCode: String,
    var threshold: Double,
    var summary: String,
    var fuelLiters: Double,
    var ratio: Double,
    var status: ManualClearanceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualClearanceManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualClearanceManifestStatus): Boolean {
        when (status) {
            ManualClearanceManifestStatus.PROPOSED -> {
                when (next) {
                    ManualClearanceManifestStatus.REJECTED, ManualClearanceManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualClearanceManifestStatus.APPROVED -> return next == ManualClearanceManifestStatus.REJECTED
            ManualClearanceManifestStatus.REJECTED -> {
                when (next) {
                    ManualClearanceManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualClearanceManifestStatus) {
        if (!canMoveTo(next)) {
            error("manual clearance manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual clearance manifest"} $code"

    fun netExposure(): Double = round((etaMinutes.toDouble() - ratio) * 100) / 100

    fun snapshot(): ManualClearanceManifest {
        return ManualClearanceManifest(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            depot = depot,
            shiftCode = shiftCode,
            threshold = threshold,
            summary = summary,
            fuelLiters = fuelLiters,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual clearance manifest in status proposed. */
fun newManualClearanceManifest(code: String, name: String): ManualClearanceManifest {
    return ManualClearanceManifest(
        code = code,
        name = name,
        etaMinutes = 0,
        depot = "",
        shiftCode = "",
        threshold = 0.0,
        summary = "",
        fuelLiters = 0.0,
        ratio = 0.0,
        status = ManualClearanceManifestStatus.PROPOSED,
    )
}
