package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute baseline is in its life cycle. */
enum class DisputeBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dispute baseline and its current status. */
data class DisputeBaseline(
    var code: String,
    var label: String,
    var capacity: Int,
    var source: String,
    var segment: String,
    var summary: String,
    var active: Boolean,
    var odometer: Int,
    var status: DisputeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeBaselineStatus): Boolean {
        when (status) {
            DisputeBaselineStatus.REQUESTED -> {
                when (next) {
                    DisputeBaselineStatus.GRANTED, DisputeBaselineStatus.REVOKED -> return true
                }
                return false
            }
            DisputeBaselineStatus.GRANTED -> return next == DisputeBaselineStatus.REVOKED
            DisputeBaselineStatus.REVOKED -> {
                when (next) {
                    DisputeBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("dispute baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((capacity.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): DisputeBaseline {
        return DisputeBaseline(
            code = code,
            label = label,
            capacity = capacity,
            source = source,
            segment = segment,
            summary = summary,
            active = active,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dispute baseline in status requested. */
fun newDisputeBaseline(code: String, label: String): DisputeBaseline {
    return DisputeBaseline(
        code = code,
        label = label,
        capacity = 0,
        source = "",
        segment = "",
        summary = "",
        active = false,
        odometer = 0,
        status = DisputeBaselineStatus.REQUESTED,
    )
}
