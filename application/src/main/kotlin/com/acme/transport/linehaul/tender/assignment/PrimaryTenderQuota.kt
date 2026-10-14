package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tender quota is in its life cycle. */
enum class PrimaryTenderQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary tender quota and its current status. */
data class PrimaryTenderQuota(
    var code: String,
    var title: String,
    var ceiling: Double,
    var externalRef: String,
    var margin: Double,
    var phone: String,
    var updatedAt: Int,
    var lane: String,
    var retries: Int,
    var version: Int,
    var label: String,
    var status: PrimaryTenderQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderQuotaStatus): Boolean {
        when (status) {
            PrimaryTenderQuotaStatus.PROPOSED -> {
                when (next) {
                    PrimaryTenderQuotaStatus.REJECTED, PrimaryTenderQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTenderQuotaStatus.APPROVED -> {
                when (next) {
                    PrimaryTenderQuotaStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryTenderQuotaStatus.REJECTED -> return next == PrimaryTenderQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryTenderQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary tender quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"primary tender quota"} $code"

    fun weightedValue(): Double = round((updatedAt.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTenderQuota {
        return PrimaryTenderQuota(
            code = code,
            title = title,
            ceiling = ceiling,
            externalRef = externalRef,
            margin = margin,
            phone = phone,
            updatedAt = updatedAt,
            lane = lane,
            retries = retries,
            version = version,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary tender quota in status proposed. */
fun newPrimaryTenderQuota(code: String, title: String): PrimaryTenderQuota {
    return PrimaryTenderQuota(
        code = code,
        title = title,
        ceiling = 0.0,
        externalRef = "",
        margin = 0.0,
        phone = "",
        updatedAt = 0,
        lane = "",
        retries = 0,
        version = 0,
        label = "",
        status = PrimaryTenderQuotaStatus.PROPOSED,
    )
}
