package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual defect claim records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a manual defect claim is in its life cycle. */
enum class ManualDefectClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual defect claim. Its code is unique within a tenant. */
data class ManualDefectClaim(
    var code: String,
    var title: String,
    var locale: String,
    var endsAt: Int,
    var loadKg: Double,
    var version: Int,
    var status: ManualDefectClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDefectClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDefectClaimStatus): Boolean {
        when (status) {
            ManualDefectClaimStatus.OPEN -> {
                when (next) {
                    ManualDefectClaimStatus.REOPENED -> return true
                }
                return false
            }
            ManualDefectClaimStatus.IN_REVIEW -> {
                when (next) {
                    ManualDefectClaimStatus.RESOLVED -> return true
                }
                return false
            }
            ManualDefectClaimStatus.RESOLVED -> return next == ManualDefectClaimStatus.REOPENED
            ManualDefectClaimStatus.REOPENED -> return next == ManualDefectClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualDefectClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual defect claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"manual defect claim"} $code"

    fun projectedValue(): Double = round((loadKg - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ManualDefectClaim {
        return ManualDefectClaim(
            code = code,
            title = title,
            locale = locale,
            endsAt = endsAt,
            loadKg = loadKg,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual defect claim in status open. */
fun newManualDefectClaim(code: String, title: String): ManualDefectClaim {
    return ManualDefectClaim(
        code = code,
        title = title,
        locale = "",
        endsAt = 0,
        loadKg = 0.0,
        version = 0,
        status = ManualDefectClaimStatus.OPEN,
    )
}
