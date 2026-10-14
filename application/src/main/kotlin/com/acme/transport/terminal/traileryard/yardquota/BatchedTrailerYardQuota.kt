package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched trailer yard quota records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a batched trailer yard quota is in its life cycle. */
enum class BatchedTrailerYardQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched trailer yard quota, with what the planners need to act on it. */
data class BatchedTrailerYardQuota(
    var code: String,
    var title: String,
    var odometer: Int,
    var contactEmail: String,
    var pinned: Boolean,
    var margin: Double,
    var region: String,
    var routeCode: String,
    var endsAt: Int,
    var attempts: Int,
    var status: BatchedTrailerYardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerYardQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerYardQuotaStatus): Boolean {
        when (status) {
            BatchedTrailerYardQuotaStatus.REQUESTED -> {
                when (next) {
                    BatchedTrailerYardQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedTrailerYardQuotaStatus.GRANTED -> {
                when (next) {
                    BatchedTrailerYardQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedTrailerYardQuotaStatus.REVOKED -> return next == BatchedTrailerYardQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedTrailerYardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer yard quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((attempts.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): BatchedTrailerYardQuota {
        return BatchedTrailerYardQuota(
            code = code,
            title = title,
            odometer = odometer,
            contactEmail = contactEmail,
            pinned = pinned,
            margin = margin,
            region = region,
            routeCode = routeCode,
            endsAt = endsAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched trailer yard quota in status requested. */
fun newBatchedTrailerYardQuota(code: String, title: String): BatchedTrailerYardQuota {
    return BatchedTrailerYardQuota(
        code = code,
        title = title,
        odometer = 0,
        contactEmail = "",
        pinned = false,
        margin = 0.0,
        region = "",
        routeCode = "",
        endsAt = 0,
        attempts = 0,
        status = BatchedTrailerYardQuotaStatus.REQUESTED,
    )
}
