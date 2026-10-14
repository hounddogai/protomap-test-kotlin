package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dispute audit is in its life cycle. */
enum class CappedDisputeAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped dispute audit and its current status. */
data class CappedDisputeAudit(
    var code: String,
    var label: String,
    var margin: Double,
    var version: Int,
    var distanceKm: Double,
    var shiftCode: String,
    var stopCount: Int,
    var odometer: Int,
    var phone: String,
    var timezone: String,
    var status: CappedDisputeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDisputeAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDisputeAuditStatus): Boolean {
        when (status) {
            CappedDisputeAuditStatus.REQUESTED -> {
                when (next) {
                    CappedDisputeAuditStatus.GRANTED, CappedDisputeAuditStatus.EXPIRED -> return true
                }
                return false
            }
            CappedDisputeAuditStatus.GRANTED -> return next == CappedDisputeAuditStatus.REVOKED || next == CappedDisputeAuditStatus.EXPIRED
            CappedDisputeAuditStatus.REVOKED -> {
                when (next) {
                    CappedDisputeAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDisputeAuditStatus) {
        if (!canMoveTo(next)) {
            error("capped dispute audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"capped dispute audit"} $code"

    fun weightedVolume(): Double = round(distanceKm * stopCount.toDouble() * 100) / 100

    fun snapshot(): CappedDisputeAudit {
        return CappedDisputeAudit(
            code = code,
            label = label,
            margin = margin,
            version = version,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            stopCount = stopCount,
            odometer = odometer,
            phone = phone,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped dispute audit in status requested. */
fun newCappedDisputeAudit(code: String, label: String): CappedDisputeAudit {
    return CappedDisputeAudit(
        code = code,
        label = label,
        margin = 0.0,
        version = 0,
        distanceKm = 0.0,
        shiftCode = "",
        stopCount = 0,
        odometer = 0,
        phone = "",
        timezone = "",
        status = CappedDisputeAuditStatus.REQUESTED,
    )
}
