package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker variance records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker variance is in its life cycle. */
enum class LockerVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a locker variance and its current status. */
data class LockerVariance(
    var code: String,
    var title: String,
    var createdAt: Int,
    var expiresAt: Int,
    var limit: Int,
    var batchSize: Int,
    var status: LockerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerVarianceStatus): Boolean {
        when (status) {
            LockerVarianceStatus.REQUESTED -> return next == LockerVarianceStatus.REVOKED
            LockerVarianceStatus.GRANTED -> return next == LockerVarianceStatus.REVOKED || next == LockerVarianceStatus.EXPIRED
            LockerVarianceStatus.REVOKED -> return next == LockerVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LockerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("locker variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((expiresAt.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): LockerVariance {
        return LockerVariance(
            code = code,
            title = title,
            createdAt = createdAt,
            expiresAt = expiresAt,
            limit = limit,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker variance in status requested. */
fun newLockerVariance(code: String, title: String): LockerVariance {
    return LockerVariance(
        code = code,
        title = title,
        createdAt = 0,
        expiresAt = 0,
        limit = 0,
        batchSize = 0,
        status = LockerVarianceStatus.REQUESTED,
    )
}
