package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute quota is in its life cycle. */
enum class DisputeQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dispute quota, with what the planners need to act on it. */
data class DisputeQuota(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var pinned: Boolean,
    var timezone: String,
    var rate: Double,
    var depot: String,
    var tags: MutableList<String>,
    var status: DisputeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeQuotaStatus): Boolean {
        when (status) {
            DisputeQuotaStatus.DRAFT -> {
                when (next) {
                    DisputeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            DisputeQuotaStatus.PENDING -> {
                when (next) {
                    DisputeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            DisputeQuotaStatus.ACTIVE -> {
                when (next) {
                    DisputeQuotaStatus.CLOSED -> return true
                }
                return false
            }
            DisputeQuotaStatus.SUSPENDED -> return next == DisputeQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DisputeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("dispute quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dispute quota"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((etaMinutes.toDouble() - rate) * 100) / 100

    fun snapshot(): DisputeQuota {
        return DisputeQuota(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            pinned = pinned,
            timezone = timezone,
            rate = rate,
            depot = depot,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dispute quota in status draft. */
fun newDisputeQuota(code: String, title: String): DisputeQuota {
    return DisputeQuota(
        code = code,
        title = title,
        etaMinutes = 0,
        pinned = false,
        timezone = "",
        rate = 0.0,
        depot = "",
        tags = mutableListOf<String>(),
        status = DisputeQuotaStatus.DRAFT,
    )
}
