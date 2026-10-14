package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt summary is in its life cycle. */
enum class AttemptSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One attempt summary, with what the planners need to act on it. */
data class AttemptSummary(
    var code: String,
    var label: String,
    var odometer: Int,
    var endsAt: Int,
    var budget: Double,
    var visible: Boolean,
    var status: AttemptSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptSummaryStatus): Boolean {
        when (status) {
            AttemptSummaryStatus.REQUESTED -> {
                when (next) {
                    AttemptSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            AttemptSummaryStatus.GRANTED -> {
                when (next) {
                    AttemptSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            AttemptSummaryStatus.REVOKED -> return next == AttemptSummaryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AttemptSummaryStatus) {
        if (!canMoveTo(next)) {
            error("attempt summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"attempt summary"} $code"

    fun effectiveValue(): Double = round(endsAt.toDouble() * budget * 100) / 100

    fun snapshot(): AttemptSummary {
        return AttemptSummary(
            code = code,
            label = label,
            odometer = odometer,
            endsAt = endsAt,
            budget = budget,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a attempt summary in status requested. */
fun newAttemptSummary(code: String, label: String): AttemptSummary {
    return AttemptSummary(
        code = code,
        label = label,
        odometer = 0,
        endsAt = 0,
        budget = 0.0,
        visible = false,
        status = AttemptSummaryStatus.REQUESTED,
    )
}
