package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul snapshot is in its life cycle. */
enum class BackhaulSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a backhaul snapshot and its current status. */
data class BackhaulSnapshot(
    var code: String,
    var name: String,
    var minimum: Double,
    var title: String,
    var carrier: String,
    var country: String,
    var odometer: Int,
    var source: String,
    var quantity: Int,
    var locale: String,
    var revision: Int,
    var status: BackhaulSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulSnapshotStatus): Boolean {
        when (status) {
            BackhaulSnapshotStatus.CREATED -> {
                when (next) {
                    BackhaulSnapshotStatus.PUBLISHED, BackhaulSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            BackhaulSnapshotStatus.VALIDATED -> return next == BackhaulSnapshotStatus.PUBLISHED || next == BackhaulSnapshotStatus.RETIRED
            BackhaulSnapshotStatus.PUBLISHED -> return next == BackhaulSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BackhaulSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("backhaul snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedValue(): Double = round((revision.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): BackhaulSnapshot {
        return BackhaulSnapshot(
            code = code,
            name = name,
            minimum = minimum,
            title = title,
            carrier = carrier,
            country = country,
            odometer = odometer,
            source = source,
            quantity = quantity,
            locale = locale,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a backhaul snapshot in status created. */
fun newBackhaulSnapshot(code: String, name: String): BackhaulSnapshot {
    return BackhaulSnapshot(
        code = code,
        name = name,
        minimum = 0.0,
        title = "",
        carrier = "",
        country = "",
        odometer = 0,
        source = "",
        quantity = 0,
        locale = "",
        revision = 0,
        status = BackhaulSnapshotStatus.CREATED,
    )
}
