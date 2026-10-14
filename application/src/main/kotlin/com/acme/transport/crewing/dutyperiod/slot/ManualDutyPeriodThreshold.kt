package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual duty period threshold is in its life cycle. */
enum class ManualDutyPeriodThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual duty period threshold as the duty period team keeps it. */
data class ManualDutyPeriodThreshold(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var visible: Boolean,
    var notes: String,
    var retries: Int,
    var status: ManualDutyPeriodThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodThresholdStatus): Boolean {
        when (status) {
            ManualDutyPeriodThresholdStatus.REQUESTED -> return next == ManualDutyPeriodThresholdStatus.REVOKED
            ManualDutyPeriodThresholdStatus.GRANTED -> {
                when (next) {
                    ManualDutyPeriodThresholdStatus.REVOKED -> return true
                }
                return false
            }
            ManualDutyPeriodThresholdStatus.REVOKED -> {
                when (next) {
                    ManualDutyPeriodThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodThresholdStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual duty period threshold"} $code"

    fun adjustedValue(): Double = round((distanceKm + retries.toDouble()) * 100) / 100

    fun snapshot(): ManualDutyPeriodThreshold {
        return ManualDutyPeriodThreshold(
            code = code,
            label = label,
            distanceKm = distanceKm,
            visible = visible,
            notes = notes,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual duty period threshold in status requested. */
fun newManualDutyPeriodThreshold(code: String, label: String): ManualDutyPeriodThreshold {
    return ManualDutyPeriodThreshold(
        code = code,
        label = label,
        distanceKm = 0.0,
        visible = false,
        notes = "",
        retries = 0,
        status = ManualDutyPeriodThresholdStatus.REQUESTED,
    )
}
