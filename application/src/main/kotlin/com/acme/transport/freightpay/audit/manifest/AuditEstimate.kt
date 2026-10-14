package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit estimate is in its life cycle. */
enum class AuditEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit estimate. Its code is unique within a tenant. */
data class AuditEstimate(
    var code: String,
    var title: String,
    var sequence: Int,
    var routeCode: String,
    var timezone: String,
    var amount: Double,
    var version: Int,
    var lane: String,
    var rate: Double,
    var active: Boolean,
    var notes: String,
    var status: AuditEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditEstimateStatus): Boolean {
        when (status) {
            AuditEstimateStatus.PROPOSED -> return next == AuditEstimateStatus.APPROVED
            AuditEstimateStatus.APPROVED -> {
                when (next) {
                    AuditEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            AuditEstimateStatus.REJECTED -> return next == AuditEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AuditEstimateStatus) {
        if (!canMoveTo(next)) {
            error("audit estimate $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round(sequence.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): AuditEstimate {
        return AuditEstimate(
            code = code,
            title = title,
            sequence = sequence,
            routeCode = routeCode,
            timezone = timezone,
            amount = amount,
            version = version,
            lane = lane,
            rate = rate,
            active = active,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a audit estimate in status proposed. */
fun newAuditEstimate(code: String, title: String): AuditEstimate {
    return AuditEstimate(
        code = code,
        title = title,
        sequence = 0,
        routeCode = "",
        timezone = "",
        amount = 0.0,
        version = 0,
        lane = "",
        rate = 0.0,
        active = false,
        notes = "",
        status = AuditEstimateStatus.PROPOSED,
    )
}
