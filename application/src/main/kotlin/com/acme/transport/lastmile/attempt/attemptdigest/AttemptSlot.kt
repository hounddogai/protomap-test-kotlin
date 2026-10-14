package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt slot is in its life cycle. */
enum class AttemptSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A attempt slot. Its code is unique within a tenant. */
data class AttemptSlot(
    var code: String,
    var title: String,
    var retries: Int,
    var externalRef: String,
    var quantity: Int,
    var tags: MutableList<String>,
    var amount: Double,
    var updatedAt: Int,
    var region: String,
    var status: AttemptSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptSlotStatus): Boolean {
        when (status) {
            AttemptSlotStatus.PROPOSED -> return next == AttemptSlotStatus.REJECTED
            AttemptSlotStatus.APPROVED -> return next == AttemptSlotStatus.REJECTED || next == AttemptSlotStatus.WITHDRAWN
            AttemptSlotStatus.REJECTED -> {
                when (next) {
                    AttemptSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptSlotStatus) {
        if (!canMoveTo(next)) {
            error("attempt slot $code cannot move from $status to $next")
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

    fun netExposure(): Double = round((quantity.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): AttemptSlot {
        return AttemptSlot(
            code = code,
            title = title,
            retries = retries,
            externalRef = externalRef,
            quantity = quantity,
            tags = tags,
            amount = amount,
            updatedAt = updatedAt,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a attempt slot in status proposed. */
fun newAttemptSlot(code: String, title: String): AttemptSlot {
    return AttemptSlot(
        code = code,
        title = title,
        retries = 0,
        externalRef = "",
        quantity = 0,
        tags = mutableListOf<String>(),
        amount = 0.0,
        updatedAt = 0,
        region = "",
        status = AttemptSlotStatus.PROPOSED,
    )
}
