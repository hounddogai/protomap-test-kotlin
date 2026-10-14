package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute digest records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute digest is in its life cycle. */
enum class DisputeDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A dispute digest. Its code is unique within a tenant. */
data class DisputeDigest(
    var code: String,
    var title: String,
    var label: String,
    var attempts: Int,
    var endsAt: Int,
    var category: String,
    var externalRef: String,
    var status: DisputeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeDigestStatus): Boolean {
        when (status) {
            DisputeDigestStatus.REQUESTED -> {
                when (next) {
                    DisputeDigestStatus.GRANTED -> return true
                }
                return false
            }
            DisputeDigestStatus.GRANTED -> {
                when (next) {
                    DisputeDigestStatus.REVOKED, DisputeDigestStatus.EXPIRED -> return true
                }
                return false
            }
            DisputeDigestStatus.REVOKED -> {
                when (next) {
                    DisputeDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeDigestStatus) {
        if (!canMoveTo(next)) {
            error("dispute digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedLoad(): Double = round((endsAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): DisputeDigest {
        return DisputeDigest(
            code = code,
            title = title,
            label = label,
            attempts = attempts,
            endsAt = endsAt,
            category = category,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dispute digest in status requested. */
fun newDisputeDigest(code: String, title: String): DisputeDigest {
    return DisputeDigest(
        code = code,
        title = title,
        label = "",
        attempts = 0,
        endsAt = 0,
        category = "",
        externalRef = "",
        status = DisputeDigestStatus.REQUESTED,
    )
}
