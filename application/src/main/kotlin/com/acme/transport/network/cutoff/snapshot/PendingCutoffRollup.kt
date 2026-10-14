package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cutoff rollup is in its life cycle. */
enum class PendingCutoffRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending cutoff rollup. Its code is unique within a tenant. */
data class PendingCutoffRollup(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var ratio: Double,
    var contactEmail: String,
    var version: Int,
    var capacity: Int,
    var status: PendingCutoffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCutoffRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCutoffRollupStatus): Boolean {
        when (status) {
            PendingCutoffRollupStatus.OPEN -> return next == PendingCutoffRollupStatus.RESOLVED
            PendingCutoffRollupStatus.IN_REVIEW -> return next == PendingCutoffRollupStatus.RESOLVED || next == PendingCutoffRollupStatus.REOPENED
            PendingCutoffRollupStatus.RESOLVED -> return next == PendingCutoffRollupStatus.ARCHIVED
            PendingCutoffRollupStatus.REOPENED -> {
                when (next) {
                    PendingCutoffRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCutoffRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending cutoff rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"pending cutoff rollup"} $code"

    fun adjustedValue(): Double = round((capacity.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PendingCutoffRollup {
        return PendingCutoffRollup(
            code = code,
            label = label,
            expiresAt = expiresAt,
            ratio = ratio,
            contactEmail = contactEmail,
            version = version,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending cutoff rollup in status open. */
fun newPendingCutoffRollup(code: String, label: String): PendingCutoffRollup {
    return PendingCutoffRollup(
        code = code,
        label = label,
        expiresAt = 0,
        ratio = 0.0,
        contactEmail = "",
        version = 0,
        capacity = 0,
        status = PendingCutoffRollupStatus.OPEN,
    )
}
