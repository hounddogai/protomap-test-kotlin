package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy fuel surcharge snapshot records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a legacy fuel surcharge snapshot is in its life cycle. */
enum class LegacyFuelSurchargeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy fuel surcharge snapshot and its current status. */
data class LegacyFuelSurchargeSnapshot(
    var code: String,
    var title: String,
    var loadKg: Double,
    var budget: Double,
    var lane: String,
    var batchSize: Int,
    var visible: Boolean,
    var active: Boolean,
    var status: LegacyFuelSurchargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFuelSurchargeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFuelSurchargeSnapshotStatus): Boolean {
        when (status) {
            LegacyFuelSurchargeSnapshotStatus.CREATED -> return next == LegacyFuelSurchargeSnapshotStatus.PUBLISHED || next == LegacyFuelSurchargeSnapshotStatus.RETIRED
            LegacyFuelSurchargeSnapshotStatus.VALIDATED -> return next == LegacyFuelSurchargeSnapshotStatus.PUBLISHED || next == LegacyFuelSurchargeSnapshotStatus.RETIRED
            LegacyFuelSurchargeSnapshotStatus.PUBLISHED -> return next == LegacyFuelSurchargeSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyFuelSurchargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy fuel surcharge snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((batchSize.toDouble() - loadKg) * 100) / 100

    fun snapshot(): LegacyFuelSurchargeSnapshot {
        return LegacyFuelSurchargeSnapshot(
            code = code,
            title = title,
            loadKg = loadKg,
            budget = budget,
            lane = lane,
            batchSize = batchSize,
            visible = visible,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy fuel surcharge snapshot in status created. */
fun newLegacyFuelSurchargeSnapshot(code: String, title: String): LegacyFuelSurchargeSnapshot {
    return LegacyFuelSurchargeSnapshot(
        code = code,
        title = title,
        loadKg = 0.0,
        budget = 0.0,
        lane = "",
        batchSize = 0,
        visible = false,
        active = false,
        status = LegacyFuelSurchargeSnapshotStatus.CREATED,
    )
}
