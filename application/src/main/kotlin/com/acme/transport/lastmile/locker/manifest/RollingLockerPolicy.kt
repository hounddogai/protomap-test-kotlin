package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling locker policy is in its life cycle. */
enum class RollingLockerPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling locker policy and its current status. */
data class RollingLockerPolicy(
    var code: String,
    var title: String,
    var createdAt: Int,
    var dock: String,
    var pallets: Int,
    var status: RollingLockerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLockerPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLockerPolicyStatus): Boolean {
        when (status) {
            RollingLockerPolicyStatus.REQUESTED -> {
                when (next) {
                    RollingLockerPolicyStatus.REVOKED -> return true
                }
                return false
            }
            RollingLockerPolicyStatus.GRANTED -> return next == RollingLockerPolicyStatus.EXPIRED
            RollingLockerPolicyStatus.REVOKED -> return next == RollingLockerPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingLockerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling locker policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((pallets.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RollingLockerPolicy {
        return RollingLockerPolicy(
            code = code,
            title = title,
            createdAt = createdAt,
            dock = dock,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling locker policy in status requested. */
fun newRollingLockerPolicy(code: String, title: String): RollingLockerPolicy {
    return RollingLockerPolicy(
        code = code,
        title = title,
        createdAt = 0,
        dock = "",
        pallets = 0,
        status = RollingLockerPolicyStatus.REQUESTED,
    )
}
