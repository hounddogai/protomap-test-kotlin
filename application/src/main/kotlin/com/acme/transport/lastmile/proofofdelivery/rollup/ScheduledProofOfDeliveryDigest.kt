package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled proof of delivery digest is in its life cycle. */
enum class ScheduledProofOfDeliveryDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled proof of delivery digest. Its code is unique within a tenant. */
data class ScheduledProofOfDeliveryDigest(
    var code: String,
    var title: String,
    var approved: Boolean,
    var segment: String,
    var priority: Int,
    var sequence: Int,
    var attempts: Int,
    var category: String,
    var ownerId: String,
    var timezone: String,
    var revision: Int,
    var status: ScheduledProofOfDeliveryDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledProofOfDeliveryDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledProofOfDeliveryDigestStatus): Boolean {
        when (status) {
            ScheduledProofOfDeliveryDigestStatus.DRAFT -> {
                when (next) {
                    ScheduledProofOfDeliveryDigestStatus.PENDING, ScheduledProofOfDeliveryDigestStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledProofOfDeliveryDigestStatus.PENDING -> {
                when (next) {
                    ScheduledProofOfDeliveryDigestStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledProofOfDeliveryDigestStatus.ACTIVE -> {
                when (next) {
                    ScheduledProofOfDeliveryDigestStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledProofOfDeliveryDigestStatus.SUSPENDED -> return next == ScheduledProofOfDeliveryDigestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledProofOfDeliveryDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled proof of delivery digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"scheduled proof of delivery digest"} $code"

    fun netValue(): Double = round(attempts.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): ScheduledProofOfDeliveryDigest {
        return ScheduledProofOfDeliveryDigest(
            code = code,
            title = title,
            approved = approved,
            segment = segment,
            priority = priority,
            sequence = sequence,
            attempts = attempts,
            category = category,
            ownerId = ownerId,
            timezone = timezone,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled proof of delivery digest in status draft. */
fun newScheduledProofOfDeliveryDigest(code: String, title: String): ScheduledProofOfDeliveryDigest {
    return ScheduledProofOfDeliveryDigest(
        code = code,
        title = title,
        approved = false,
        segment = "",
        priority = 0,
        sequence = 0,
        attempts = 0,
        category = "",
        ownerId = "",
        timezone = "",
        revision = 0,
        status = ScheduledProofOfDeliveryDigestStatus.DRAFT,
    )
}
