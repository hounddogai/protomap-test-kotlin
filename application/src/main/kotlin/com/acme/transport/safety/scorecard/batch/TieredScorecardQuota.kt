package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered scorecard quota is in its life cycle. */
enum class TieredScorecardQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered scorecard quota and its current status. */
data class TieredScorecardQuota(
    var code: String,
    var name: String,
    var source: String,
    var contactEmail: String,
    var phone: String,
    var discount: Double,
    var capacity: Int,
    var currency: String,
    var updatedAt: Int,
    var segment: String,
    var status: TieredScorecardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredScorecardQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredScorecardQuotaStatus): Boolean {
        when (status) {
            TieredScorecardQuotaStatus.PLANNED -> {
                when (next) {
                    TieredScorecardQuotaStatus.SCHEDULED, TieredScorecardQuotaStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredScorecardQuotaStatus.SCHEDULED -> {
                when (next) {
                    TieredScorecardQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            TieredScorecardQuotaStatus.IN_PROGRESS -> return next == TieredScorecardQuotaStatus.COMPLETED
            TieredScorecardQuotaStatus.COMPLETED -> {
                when (next) {
                    TieredScorecardQuotaStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredScorecardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tiered scorecard quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((updatedAt.toDouble() - discount) * 100) / 100

    fun snapshot(): TieredScorecardQuota {
        return TieredScorecardQuota(
            code = code,
            name = name,
            source = source,
            contactEmail = contactEmail,
            phone = phone,
            discount = discount,
            capacity = capacity,
            currency = currency,
            updatedAt = updatedAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered scorecard quota in status planned. */
fun newTieredScorecardQuota(code: String, name: String): TieredScorecardQuota {
    return TieredScorecardQuota(
        code = code,
        name = name,
        source = "",
        contactEmail = "",
        phone = "",
        discount = 0.0,
        capacity = 0,
        currency = "",
        updatedAt = 0,
        segment = "",
        status = TieredScorecardQuotaStatus.PLANNED,
    )
}
