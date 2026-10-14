package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy attempt audit is in its life cycle. */
enum class LegacyAttemptAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy attempt audit as the attempt team keeps it. */
data class LegacyAttemptAudit(
    var code: String,
    var title: String,
    var retries: Int,
    var carrier: String,
    var fuelLiters: Double,
    var name: String,
    var expiresAt: Int,
    var quantity: Int,
    var approved: Boolean,
    var status: LegacyAttemptAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAttemptAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAttemptAuditStatus): Boolean {
        when (status) {
            LegacyAttemptAuditStatus.CREATED -> {
                when (next) {
                    LegacyAttemptAuditStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyAttemptAuditStatus.VALIDATED -> return next == LegacyAttemptAuditStatus.RETIRED
            LegacyAttemptAuditStatus.PUBLISHED -> {
                when (next) {
                    LegacyAttemptAuditStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAttemptAuditStatus) {
        if (!canMoveTo(next)) {
            error("legacy attempt audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((quantity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LegacyAttemptAudit {
        return LegacyAttemptAudit(
            code = code,
            title = title,
            retries = retries,
            carrier = carrier,
            fuelLiters = fuelLiters,
            name = name,
            expiresAt = expiresAt,
            quantity = quantity,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy attempt audit in status created. */
fun newLegacyAttemptAudit(code: String, title: String): LegacyAttemptAudit {
    return LegacyAttemptAudit(
        code = code,
        title = title,
        retries = 0,
        carrier = "",
        fuelLiters = 0.0,
        name = "",
        expiresAt = 0,
        quantity = 0,
        approved = false,
        status = LegacyAttemptAuditStatus.CREATED,
    )
}
