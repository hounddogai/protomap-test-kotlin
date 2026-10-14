package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss summary is in its life cycle. */
enum class NearMissSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One near miss summary, with what the planners need to act on it. */
data class NearMissSummary(
    var code: String,
    var name: String,
    var cost: Double,
    var limit: Int,
    var revision: Int,
    var version: Int,
    var visible: Boolean,
    var status: NearMissSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissSummaryStatus): Boolean {
        when (status) {
            NearMissSummaryStatus.PROPOSED -> return next == NearMissSummaryStatus.APPROVED
            NearMissSummaryStatus.APPROVED -> return next == NearMissSummaryStatus.REJECTED || next == NearMissSummaryStatus.WITHDRAWN
            NearMissSummaryStatus.REJECTED -> return next == NearMissSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NearMissSummaryStatus) {
        if (!canMoveTo(next)) {
            error("near miss summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"near miss summary"} $code"

    fun effectiveVolume(): Double = round(cost * limit.toDouble() * 100) / 100

    fun snapshot(): NearMissSummary {
        return NearMissSummary(
            code = code,
            name = name,
            cost = cost,
            limit = limit,
            revision = revision,
            version = version,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a near miss summary in status proposed. */
fun newNearMissSummary(code: String, name: String): NearMissSummary {
    return NearMissSummary(
        code = code,
        name = name,
        cost = 0.0,
        limit = 0,
        revision = 0,
        version = 0,
        visible = false,
        status = NearMissSummaryStatus.PROPOSED,
    )
}
