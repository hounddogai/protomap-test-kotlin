package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dispute summary is in its life cycle. */
enum class ManualDisputeSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual dispute summary as the dispute team keeps it. */
data class ManualDisputeSummary(
    var code: String,
    var label: String,
    var limit: Int,
    var etaMinutes: Int,
    var notes: String,
    var status: ManualDisputeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDisputeSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDisputeSummaryStatus): Boolean {
        when (status) {
            ManualDisputeSummaryStatus.IDLE -> return next == ManualDisputeSummaryStatus.READY || next == ManualDisputeSummaryStatus.DRAINING
            ManualDisputeSummaryStatus.WARMING -> return next == ManualDisputeSummaryStatus.READY || next == ManualDisputeSummaryStatus.DRAINING
            ManualDisputeSummaryStatus.READY -> return next == ManualDisputeSummaryStatus.STOPPED
            ManualDisputeSummaryStatus.DRAINING -> {
                when (next) {
                    ManualDisputeSummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDisputeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual dispute summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual dispute summary"} $code"

    fun projectedTotal(): Double = round((etaMinutes.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): ManualDisputeSummary {
        return ManualDisputeSummary(
            code = code,
            label = label,
            limit = limit,
            etaMinutes = etaMinutes,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual dispute summary in status idle. */
fun newManualDisputeSummary(code: String, label: String): ManualDisputeSummary {
    return ManualDisputeSummary(
        code = code,
        label = label,
        limit = 0,
        etaMinutes = 0,
        notes = "",
        status = ManualDisputeSummaryStatus.IDLE,
    )
}
