package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period adjustment is in its life cycle. */
enum class DutyPeriodAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A duty period adjustment. Its code is unique within a tenant. */
data class DutyPeriodAdjustment(
    var code: String,
    var label: String,
    var channel: String,
    var rate: Double,
    var discount: Double,
    var pallets: Int,
    var vehicleId: String,
    var source: String,
    var segment: String,
    var status: DutyPeriodAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodAdjustmentStatus): Boolean {
        when (status) {
            DutyPeriodAdjustmentStatus.DRAFT -> return next == DutyPeriodAdjustmentStatus.PENDING
            DutyPeriodAdjustmentStatus.PENDING -> return next == DutyPeriodAdjustmentStatus.CLOSED
            DutyPeriodAdjustmentStatus.ACTIVE -> return next == DutyPeriodAdjustmentStatus.CLOSED
            DutyPeriodAdjustmentStatus.SUSPENDED -> {
                when (next) {
                    DutyPeriodAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("duty period adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"duty period adjustment"} $code"

    fun weightedVolume(): Double = round((discount + pallets.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodAdjustment {
        return DutyPeriodAdjustment(
            code = code,
            label = label,
            channel = channel,
            rate = rate,
            discount = discount,
            pallets = pallets,
            vehicleId = vehicleId,
            source = source,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a duty period adjustment in status draft. */
fun newDutyPeriodAdjustment(code: String, label: String): DutyPeriodAdjustment {
    return DutyPeriodAdjustment(
        code = code,
        label = label,
        channel = "",
        rate = 0.0,
        discount = 0.0,
        pallets = 0,
        vehicleId = "",
        source = "",
        segment = "",
        status = DutyPeriodAdjustmentStatus.DRAFT,
    )
}
