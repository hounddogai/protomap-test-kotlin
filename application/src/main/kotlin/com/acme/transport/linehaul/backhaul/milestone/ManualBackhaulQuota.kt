package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual backhaul quota is in its life cycle. */
enum class ManualBackhaulQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual backhaul quota and its current status. */
data class ManualBackhaulQuota(
    var code: String,
    var label: String,
    var capacity: Int,
    var pallets: Int,
    var segment: String,
    var threshold: Double,
    var carrier: String,
    var floorValue: Double,
    var visible: Boolean,
    var endsAt: Int,
    var status: ManualBackhaulQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulQuotaStatus): Boolean {
        when (status) {
            ManualBackhaulQuotaStatus.PLANNED -> return next == ManualBackhaulQuotaStatus.COMPLETED || next == ManualBackhaulQuotaStatus.CANCELLED
            ManualBackhaulQuotaStatus.SCHEDULED -> return next == ManualBackhaulQuotaStatus.IN_PROGRESS || next == ManualBackhaulQuotaStatus.CANCELLED
            ManualBackhaulQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    ManualBackhaulQuotaStatus.COMPLETED, ManualBackhaulQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            ManualBackhaulQuotaStatus.COMPLETED -> return next == ManualBackhaulQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualBackhaulQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((threshold + floorValue) * 100) / 100

    fun snapshot(): ManualBackhaulQuota {
        return ManualBackhaulQuota(
            code = code,
            label = label,
            capacity = capacity,
            pallets = pallets,
            segment = segment,
            threshold = threshold,
            carrier = carrier,
            floorValue = floorValue,
            visible = visible,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual backhaul quota in status planned. */
fun newManualBackhaulQuota(code: String, label: String): ManualBackhaulQuota {
    return ManualBackhaulQuota(
        code = code,
        label = label,
        capacity = 0,
        pallets = 0,
        segment = "",
        threshold = 0.0,
        carrier = "",
        floorValue = 0.0,
        visible = false,
        endsAt = 0,
        status = ManualBackhaulQuotaStatus.PLANNED,
    )
}
