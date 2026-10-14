package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.normalize

/** Where a attempt bundle is in its life cycle. */
enum class AttemptBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A attempt bundle as the attempt team keeps it. */
data class AttemptBundle(
    var code: String,
    var name: String,
    var batchSize: Int,
    var phone: String,
    var pinned: Boolean,
    var depot: String,
    var status: AttemptBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptBundleStatus): Boolean {
        when (status) {
            AttemptBundleStatus.PROPOSED -> {
                when (next) {
                    AttemptBundleStatus.APPROVED, AttemptBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AttemptBundleStatus.APPROVED -> return next == AttemptBundleStatus.REJECTED || next == AttemptBundleStatus.WITHDRAWN
            AttemptBundleStatus.REJECTED -> {
                when (next) {
                    AttemptBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptBundleStatus) {
        if (!canMoveTo(next)) {
            error("attempt bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): AttemptBundle {
        return AttemptBundle(
            code = code,
            name = name,
            batchSize = batchSize,
            phone = phone,
            pinned = pinned,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a attempt bundle in status proposed. */
fun newAttemptBundle(code: String, name: String): AttemptBundle {
    return AttemptBundle(
        code = code,
        name = name,
        batchSize = 0,
        phone = "",
        pinned = false,
        depot = "",
        status = AttemptBundleStatus.PROPOSED,
    )
}
