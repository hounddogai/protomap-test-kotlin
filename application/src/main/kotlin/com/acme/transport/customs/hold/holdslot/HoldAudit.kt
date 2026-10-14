package com.acme.transport.customs.hold.holdslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold audit is in its life cycle. */
enum class HoldAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a hold audit and its current status. */
data class HoldAudit(
    var code: String,
    var name: String,
    var floorValue: Double,
    var segment: String,
    var updatedAt: Int,
    var expiresAt: Int,
    var discount: Double,
    var approved: Boolean,
    var threshold: Double,
    var amount: Double,
    var etaMinutes: Int,
    var status: HoldAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldAuditStatus): Boolean {
        when (status) {
            HoldAuditStatus.REQUESTED -> {
                when (next) {
                    HoldAuditStatus.REVOKED -> return true
                }
                return false
            }
            HoldAuditStatus.GRANTED -> return next == HoldAuditStatus.REVOKED || next == HoldAuditStatus.EXPIRED
            HoldAuditStatus.REVOKED -> {
                when (next) {
                    HoldAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldAuditStatus) {
        if (!canMoveTo(next)) {
            error("hold audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((updatedAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): HoldAudit {
        return HoldAudit(
            code = code,
            name = name,
            floorValue = floorValue,
            segment = segment,
            updatedAt = updatedAt,
            expiresAt = expiresAt,
            discount = discount,
            approved = approved,
            threshold = threshold,
            amount = amount,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a hold audit in status requested. */
fun newHoldAudit(code: String, name: String): HoldAudit {
    return HoldAudit(
        code = code,
        name = name,
        floorValue = 0.0,
        segment = "",
        updatedAt = 0,
        expiresAt = 0,
        discount = 0.0,
        approved = false,
        threshold = 0.0,
        amount = 0.0,
        etaMinutes = 0,
        status = HoldAuditStatus.REQUESTED,
    )
}
