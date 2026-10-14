package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time quota is in its life cycle. */
enum class TransitTimeQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A transit time quota. Its code is unique within a tenant. */
data class TransitTimeQuota(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var tags: MutableList<String>,
    var rate: Double,
    var maximum: Double,
    var amount: Double,
    var etaMinutes: Int,
    var status: TransitTimeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeQuotaStatus): Boolean {
        when (status) {
            TransitTimeQuotaStatus.DRAFT -> return next == TransitTimeQuotaStatus.ACTIVE || next == TransitTimeQuotaStatus.SUSPENDED
            TransitTimeQuotaStatus.PENDING -> {
                when (next) {
                    TransitTimeQuotaStatus.SUSPENDED, TransitTimeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimeQuotaStatus.ACTIVE -> return next == TransitTimeQuotaStatus.SUSPENDED || next == TransitTimeQuotaStatus.CLOSED
            TransitTimeQuotaStatus.SUSPENDED -> {
                when (next) {
                    TransitTimeQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("transit time quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((rate - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeQuota {
        return TransitTimeQuota(
            code = code,
            title = title,
            updatedAt = updatedAt,
            tags = tags,
            rate = rate,
            maximum = maximum,
            amount = amount,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time quota in status draft. */
fun newTransitTimeQuota(code: String, title: String): TransitTimeQuota {
    return TransitTimeQuota(
        code = code,
        title = title,
        updatedAt = 0,
        tags = mutableListOf<String>(),
        rate = 0.0,
        maximum = 0.0,
        amount = 0.0,
        etaMinutes = 0,
        status = TransitTimeQuotaStatus.DRAFT,
    )
}
