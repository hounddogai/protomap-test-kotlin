package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery agreement is in its life cycle. */
enum class RedeliveryAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A redelivery agreement as the redelivery team keeps it. */
data class RedeliveryAgreement(
    var code: String,
    var title: String,
    var limit: Int,
    var ceiling: Double,
    var lane: String,
    var source: String,
    var channel: String,
    var createdAt: Int,
    var approved: Boolean,
    var status: RedeliveryAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryAgreementStatus): Boolean {
        when (status) {
            RedeliveryAgreementStatus.CREATED -> return next == RedeliveryAgreementStatus.VALIDATED || next == RedeliveryAgreementStatus.PUBLISHED
            RedeliveryAgreementStatus.VALIDATED -> {
                when (next) {
                    RedeliveryAgreementStatus.RETIRED -> return true
                }
                return false
            }
            RedeliveryAgreementStatus.PUBLISHED -> return next == RedeliveryAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RedeliveryAgreementStatus) {
        if (!canMoveTo(next)) {
            error("redelivery agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((createdAt.toDouble() + ceiling) * 100) / 100

    fun snapshot(): RedeliveryAgreement {
        return RedeliveryAgreement(
            code = code,
            title = title,
            limit = limit,
            ceiling = ceiling,
            lane = lane,
            source = source,
            channel = channel,
            createdAt = createdAt,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a redelivery agreement in status created. */
fun newRedeliveryAgreement(code: String, title: String): RedeliveryAgreement {
    return RedeliveryAgreement(
        code = code,
        title = title,
        limit = 0,
        ceiling = 0.0,
        lane = "",
        source = "",
        channel = "",
        createdAt = 0,
        approved = false,
        status = RedeliveryAgreementStatus.CREATED,
    )
}
