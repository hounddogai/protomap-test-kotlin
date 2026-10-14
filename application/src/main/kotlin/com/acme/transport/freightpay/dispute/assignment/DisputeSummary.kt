package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute summary is in its life cycle. */
enum class DisputeSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dispute summary. Its code is unique within a tenant. */
data class DisputeSummary(
    var code: String,
    var label: String,
    var discount: Double,
    var capacity: Int,
    var driverId: String,
    var version: Int,
    var status: DisputeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeSummaryStatus): Boolean {
        when (status) {
            DisputeSummaryStatus.NEW -> {
                when (next) {
                    DisputeSummaryStatus.RUNNING, DisputeSummaryStatus.FAILED -> return true
                }
                return false
            }
            DisputeSummaryStatus.QUEUED -> return next == DisputeSummaryStatus.FAILED
            DisputeSummaryStatus.RUNNING -> {
                when (next) {
                    DisputeSummaryStatus.DONE -> return true
                }
                return false
            }
            DisputeSummaryStatus.DONE -> {
                when (next) {
                    DisputeSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("dispute summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"dispute summary"} $code"

    fun effectiveLoad(): Double = round((capacity.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): DisputeSummary {
        return DisputeSummary(
            code = code,
            label = label,
            discount = discount,
            capacity = capacity,
            driverId = driverId,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dispute summary in status new. */
fun newDisputeSummary(code: String, label: String): DisputeSummary {
    return DisputeSummary(
        code = code,
        label = label,
        discount = 0.0,
        capacity = 0,
        driverId = "",
        version = 0,
        status = DisputeSummaryStatus.NEW,
    )
}
