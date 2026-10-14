package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect claim records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect claim is in its life cycle. */
enum class DefectClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a defect claim and its current status. */
data class DefectClaim(
    var code: String,
    var title: String,
    var version: Int,
    var discount: Double,
    var threshold: Double,
    var graceDays: Int,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var locale: String,
    var updatedAt: Int,
    var depot: String,
    var status: DefectClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectClaimStatus): Boolean {
        when (status) {
            DefectClaimStatus.OPEN -> return next == DefectClaimStatus.ARCHIVED
            DefectClaimStatus.IN_REVIEW -> return next == DefectClaimStatus.ARCHIVED
            DefectClaimStatus.RESOLVED -> return next == DefectClaimStatus.REOPENED || next == DefectClaimStatus.ARCHIVED
            DefectClaimStatus.REOPENED -> return next == DefectClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefectClaimStatus) {
        if (!canMoveTo(next)) {
            error("defect claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"defect claim"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round(updatedAt.toDouble() * threshold * 100) / 100

    fun snapshot(): DefectClaim {
        return DefectClaim(
            code = code,
            title = title,
            version = version,
            discount = discount,
            threshold = threshold,
            graceDays = graceDays,
            externalRef = externalRef,
            attributes = attributes,
            locale = locale,
            updatedAt = updatedAt,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a defect claim in status open. */
fun newDefectClaim(code: String, title: String): DefectClaim {
    return DefectClaim(
        code = code,
        title = title,
        version = 0,
        discount = 0.0,
        threshold = 0.0,
        graceDays = 0,
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        locale = "",
        updatedAt = 0,
        depot = "",
        status = DefectClaimStatus.OPEN,
    )
}
