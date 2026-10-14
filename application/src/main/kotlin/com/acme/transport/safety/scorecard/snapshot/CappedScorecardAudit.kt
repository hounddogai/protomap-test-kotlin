package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped scorecard audit is in its life cycle. */
enum class CappedScorecardAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped scorecard audit. Its code is unique within a tenant. */
data class CappedScorecardAudit(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var source: String,
    var country: String,
    var distanceKm: Double,
    var windowDays: Int,
    var version: Int,
    var discount: Double,
    var status: CappedScorecardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardAuditStatus): Boolean {
        when (status) {
            CappedScorecardAuditStatus.PLANNED -> {
                when (next) {
                    CappedScorecardAuditStatus.SCHEDULED -> return true
                }
                return false
            }
            CappedScorecardAuditStatus.SCHEDULED -> {
                when (next) {
                    CappedScorecardAuditStatus.IN_PROGRESS, CappedScorecardAuditStatus.COMPLETED -> return true
                }
                return false
            }
            CappedScorecardAuditStatus.IN_PROGRESS -> {
                when (next) {
                    CappedScorecardAuditStatus.CANCELLED -> return true
                }
                return false
            }
            CappedScorecardAuditStatus.COMPLETED -> {
                when (next) {
                    CappedScorecardAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedScorecardAuditStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round((windowDays.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): CappedScorecardAudit {
        return CappedScorecardAudit(
            code = code,
            name = name,
            pinned = pinned,
            source = source,
            country = country,
            distanceKm = distanceKm,
            windowDays = windowDays,
            version = version,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped scorecard audit in status planned. */
fun newCappedScorecardAudit(code: String, name: String): CappedScorecardAudit {
    return CappedScorecardAudit(
        code = code,
        name = name,
        pinned = false,
        source = "",
        country = "",
        distanceKm = 0.0,
        windowDays = 0,
        version = 0,
        discount = 0.0,
        status = CappedScorecardAuditStatus.PLANNED,
    )
}
