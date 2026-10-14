package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling lease return summary is in its life cycle. */
enum class RollingLeaseReturnSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling lease return summary. Its code is unique within a tenant. */
data class RollingLeaseReturnSummary(
    var code: String,
    var title: String,
    var ceiling: Double,
    var source: String,
    var revision: Int,
    var status: RollingLeaseReturnSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLeaseReturnSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLeaseReturnSummaryStatus): Boolean {
        when (status) {
            RollingLeaseReturnSummaryStatus.PLANNED -> {
                when (next) {
                    RollingLeaseReturnSummaryStatus.SCHEDULED, RollingLeaseReturnSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RollingLeaseReturnSummaryStatus.SCHEDULED -> return next == RollingLeaseReturnSummaryStatus.IN_PROGRESS
            RollingLeaseReturnSummaryStatus.IN_PROGRESS -> return next == RollingLeaseReturnSummaryStatus.COMPLETED || next == RollingLeaseReturnSummaryStatus.CANCELLED
            RollingLeaseReturnSummaryStatus.COMPLETED -> {
                when (next) {
                    RollingLeaseReturnSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingLeaseReturnSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rolling lease return summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((ceiling + revision.toDouble()) * 100) / 100

    fun snapshot(): RollingLeaseReturnSummary {
        return RollingLeaseReturnSummary(
            code = code,
            title = title,
            ceiling = ceiling,
            source = source,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling lease return summary in status planned. */
fun newRollingLeaseReturnSummary(code: String, title: String): RollingLeaseReturnSummary {
    return RollingLeaseReturnSummary(
        code = code,
        title = title,
        ceiling = 0.0,
        source = "",
        revision = 0,
        status = RollingLeaseReturnSummaryStatus.PLANNED,
    )
}
