package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.normalize

/** Where a lenient dispute tier is in its life cycle. */
enum class LenientDisputeTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient dispute tier as the dispute team keeps it. */
data class LenientDisputeTier(
    var code: String,
    var title: String,
    var driverId: String,
    var timezone: String,
    var label: String,
    var amount: Double,
    var status: LenientDisputeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputeTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputeTierStatus): Boolean {
        when (status) {
            LenientDisputeTierStatus.NEW -> return next == LenientDisputeTierStatus.FAILED
            LenientDisputeTierStatus.QUEUED -> {
                when (next) {
                    LenientDisputeTierStatus.FAILED -> return true
                }
                return false
            }
            LenientDisputeTierStatus.RUNNING -> return next == LenientDisputeTierStatus.DONE
            LenientDisputeTierStatus.DONE -> return next == LenientDisputeTierStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientDisputeTierStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun measuredValue(): Double = amount

    fun snapshot(): LenientDisputeTier {
        return LenientDisputeTier(
            code = code,
            title = title,
            driverId = driverId,
            timezone = timezone,
            label = label,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient dispute tier in status new. */
fun newLenientDisputeTier(code: String, title: String): LenientDisputeTier {
    return LenientDisputeTier(
        code = code,
        title = title,
        driverId = "",
        timezone = "",
        label = "",
        amount = 0.0,
        status = LenientDisputeTierStatus.NEW,
    )
}
