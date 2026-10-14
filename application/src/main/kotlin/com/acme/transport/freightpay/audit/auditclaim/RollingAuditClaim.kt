package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling audit claim records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a rolling audit claim is in its life cycle. */
enum class RollingAuditClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rolling audit claim and its current status. */
data class RollingAuditClaim(
    var code: String,
    var name: String,
    var lane: String,
    var phone: String,
    var endsAt: Int,
    var loadKg: Double,
    var budget: Double,
    var carrier: String,
    var etaMinutes: Int,
    var status: RollingAuditClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAuditClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAuditClaimStatus): Boolean {
        when (status) {
            RollingAuditClaimStatus.NEW -> {
                when (next) {
                    RollingAuditClaimStatus.DONE, RollingAuditClaimStatus.FAILED -> return true
                }
                return false
            }
            RollingAuditClaimStatus.QUEUED -> return next == RollingAuditClaimStatus.RUNNING
            RollingAuditClaimStatus.RUNNING -> {
                when (next) {
                    RollingAuditClaimStatus.DONE -> return true
                }
                return false
            }
            RollingAuditClaimStatus.DONE -> {
                when (next) {
                    RollingAuditClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAuditClaimStatus) {
        if (!canMoveTo(next)) {
            error("rolling audit claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round(etaMinutes.toDouble() * budget * 100) / 100

    fun snapshot(): RollingAuditClaim {
        return RollingAuditClaim(
            code = code,
            name = name,
            lane = lane,
            phone = phone,
            endsAt = endsAt,
            loadKg = loadKg,
            budget = budget,
            carrier = carrier,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling audit claim in status new. */
fun newRollingAuditClaim(code: String, name: String): RollingAuditClaim {
    return RollingAuditClaim(
        code = code,
        name = name,
        lane = "",
        phone = "",
        endsAt = 0,
        loadKg = 0.0,
        budget = 0.0,
        carrier = "",
        etaMinutes = 0,
        status = RollingAuditClaimStatus.NEW,
    )
}
