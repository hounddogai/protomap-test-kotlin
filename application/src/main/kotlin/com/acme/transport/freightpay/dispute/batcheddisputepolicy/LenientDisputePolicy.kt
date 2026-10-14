package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dispute policy records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a lenient dispute policy is in its life cycle. */
enum class LenientDisputePolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient dispute policy as the dispute team keeps it. */
data class LenientDisputePolicy(
    var code: String,
    var title: String,
    var budget: Double,
    var dock: String,
    var pallets: Int,
    var expiresAt: Int,
    var revision: Int,
    var status: LenientDisputePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputePolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputePolicyStatus): Boolean {
        when (status) {
            LenientDisputePolicyStatus.DRAFT -> {
                when (next) {
                    LenientDisputePolicyStatus.PENDING, LenientDisputePolicyStatus.CLOSED -> return true
                }
                return false
            }
            LenientDisputePolicyStatus.PENDING -> return next == LenientDisputePolicyStatus.CLOSED
            LenientDisputePolicyStatus.ACTIVE -> {
                when (next) {
                    LenientDisputePolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientDisputePolicyStatus.SUSPENDED -> return next == LenientDisputePolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientDisputePolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute policy $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"lenient dispute policy"} $code"

    fun projectedVolume(): Double = round(revision.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): LenientDisputePolicy {
        return LenientDisputePolicy(
            code = code,
            title = title,
            budget = budget,
            dock = dock,
            pallets = pallets,
            expiresAt = expiresAt,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient dispute policy in status draft. */
fun newLenientDisputePolicy(code: String, title: String): LenientDisputePolicy {
    return LenientDisputePolicy(
        code = code,
        title = title,
        budget = 0.0,
        dock = "",
        pallets = 0,
        expiresAt = 0,
        revision = 0,
        status = LenientDisputePolicyStatus.DRAFT,
    )
}
