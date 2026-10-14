package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dispute quota is in its life cycle. */
enum class StrictDisputeQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict dispute quota and its current status. */
data class StrictDisputeQuota(
    var code: String,
    var title: String,
    var maximum: Double,
    var approved: Boolean,
    var threshold: Double,
    var ownerId: String,
    var status: StrictDisputeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDisputeQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDisputeQuotaStatus): Boolean {
        when (status) {
            StrictDisputeQuotaStatus.NEW -> return next == StrictDisputeQuotaStatus.QUEUED || next == StrictDisputeQuotaStatus.DONE
            StrictDisputeQuotaStatus.QUEUED -> {
                when (next) {
                    StrictDisputeQuotaStatus.DONE -> return true
                }
                return false
            }
            StrictDisputeQuotaStatus.RUNNING -> return next == StrictDisputeQuotaStatus.DONE
            StrictDisputeQuotaStatus.DONE -> {
                when (next) {
                    StrictDisputeQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDisputeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("strict dispute quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round(maximum * threshold * 100) / 100

    fun snapshot(): StrictDisputeQuota {
        return StrictDisputeQuota(
            code = code,
            title = title,
            maximum = maximum,
            approved = approved,
            threshold = threshold,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict dispute quota in status new. */
fun newStrictDisputeQuota(code: String, title: String): StrictDisputeQuota {
    return StrictDisputeQuota(
        code = code,
        title = title,
        maximum = 0.0,
        approved = false,
        threshold = 0.0,
        ownerId = "",
        status = StrictDisputeQuotaStatus.NEW,
    )
}
