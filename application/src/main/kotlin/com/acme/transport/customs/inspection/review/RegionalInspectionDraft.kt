package com.acme.transport.customs.inspection.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional inspection draft is in its life cycle. */
enum class RegionalInspectionDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional inspection draft as the inspection team keeps it. */
data class RegionalInspectionDraft(
    var code: String,
    var title: String,
    var maximum: Double,
    var vehicleId: String,
    var visible: Boolean,
    var odometer: Int,
    var currency: String,
    var channel: String,
    var status: RegionalInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalInspectionDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalInspectionDraftStatus): Boolean {
        when (status) {
            RegionalInspectionDraftStatus.IDLE -> return next == RegionalInspectionDraftStatus.READY
            RegionalInspectionDraftStatus.WARMING -> {
                when (next) {
                    RegionalInspectionDraftStatus.STOPPED -> return true
                }
                return false
            }
            RegionalInspectionDraftStatus.READY -> return next == RegionalInspectionDraftStatus.DRAINING || next == RegionalInspectionDraftStatus.STOPPED
            RegionalInspectionDraftStatus.DRAINING -> {
                when (next) {
                    RegionalInspectionDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("regional inspection draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional inspection draft"} $code"

    fun effectiveLoad(): Double = round((maximum + odometer.toDouble()) * 100) / 100

    fun snapshot(): RegionalInspectionDraft {
        return RegionalInspectionDraft(
            code = code,
            title = title,
            maximum = maximum,
            vehicleId = vehicleId,
            visible = visible,
            odometer = odometer,
            currency = currency,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional inspection draft in status idle. */
fun newRegionalInspectionDraft(code: String, title: String): RegionalInspectionDraft {
    return RegionalInspectionDraft(
        code = code,
        title = title,
        maximum = 0.0,
        vehicleId = "",
        visible = false,
        odometer = 0,
        currency = "",
        channel = "",
        status = RegionalInspectionDraftStatus.IDLE,
    )
}
