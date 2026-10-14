package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel surcharge snapshot is in its life cycle. */
enum class RegionalFuelSurchargeSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional fuel surcharge snapshot as the fuel surcharge team keeps it. */
data class RegionalFuelSurchargeSnapshot(
    var code: String,
    var name: String,
    var lane: String,
    var threshold: Double,
    var visible: Boolean,
    var revision: Int,
    var stopCount: Int,
    var locale: String,
    var status: RegionalFuelSurchargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelSurchargeSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelSurchargeSnapshotStatus): Boolean {
        when (status) {
            RegionalFuelSurchargeSnapshotStatus.NEW -> return next == RegionalFuelSurchargeSnapshotStatus.QUEUED || next == RegionalFuelSurchargeSnapshotStatus.RUNNING
            RegionalFuelSurchargeSnapshotStatus.QUEUED -> return next == RegionalFuelSurchargeSnapshotStatus.DONE || next == RegionalFuelSurchargeSnapshotStatus.FAILED
            RegionalFuelSurchargeSnapshotStatus.RUNNING -> {
                when (next) {
                    RegionalFuelSurchargeSnapshotStatus.DONE, RegionalFuelSurchargeSnapshotStatus.FAILED -> return true
                }
                return false
            }
            RegionalFuelSurchargeSnapshotStatus.DONE -> return next == RegionalFuelSurchargeSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalFuelSurchargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel surcharge snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"regional fuel surcharge snapshot"} $code"

    fun adjustedTotal(): Double = round(revision.toDouble() * threshold * 100) / 100

    fun snapshot(): RegionalFuelSurchargeSnapshot {
        return RegionalFuelSurchargeSnapshot(
            code = code,
            name = name,
            lane = lane,
            threshold = threshold,
            visible = visible,
            revision = revision,
            stopCount = stopCount,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional fuel surcharge snapshot in status new. */
fun newRegionalFuelSurchargeSnapshot(code: String, name: String): RegionalFuelSurchargeSnapshot {
    return RegionalFuelSurchargeSnapshot(
        code = code,
        name = name,
        lane = "",
        threshold = 0.0,
        visible = false,
        revision = 0,
        stopCount = 0,
        locale = "",
        status = RegionalFuelSurchargeSnapshotStatus.NEW,
    )
}
