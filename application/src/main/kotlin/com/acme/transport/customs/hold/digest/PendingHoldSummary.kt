package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold summary is in its life cycle. */
enum class PendingHoldSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending hold summary, with what the planners need to act on it. */
data class PendingHoldSummary(
    var code: String,
    var label: String,
    var retries: Int,
    var depot: String,
    var threshold: Double,
    var attempts: Int,
    var version: Int,
    var locale: String,
    var phone: String,
    var status: PendingHoldSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldSummaryStatus): Boolean {
        when (status) {
            PendingHoldSummaryStatus.REQUESTED -> {
                when (next) {
                    PendingHoldSummaryStatus.REVOKED -> return true
                }
                return false
            }
            PendingHoldSummaryStatus.GRANTED -> return next == PendingHoldSummaryStatus.EXPIRED
            PendingHoldSummaryStatus.REVOKED -> {
                when (next) {
                    PendingHoldSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHoldSummaryStatus) {
        if (!canMoveTo(next)) {
            error("pending hold summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((attempts.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): PendingHoldSummary {
        return PendingHoldSummary(
            code = code,
            label = label,
            retries = retries,
            depot = depot,
            threshold = threshold,
            attempts = attempts,
            version = version,
            locale = locale,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending hold summary in status requested. */
fun newPendingHoldSummary(code: String, label: String): PendingHoldSummary {
    return PendingHoldSummary(
        code = code,
        label = label,
        retries = 0,
        depot = "",
        threshold = 0.0,
        attempts = 0,
        version = 0,
        locale = "",
        phone = "",
        status = PendingHoldSummaryStatus.REQUESTED,
    )
}
