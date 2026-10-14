package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dwell override is in its life cycle. */
enum class ManualDwellOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual dwell override. Its code is unique within a tenant. */
data class ManualDwellOverride(
    var code: String,
    var name: String,
    var limit: Int,
    var floorValue: Double,
    var discount: Double,
    var pinned: Boolean,
    var maximum: Double,
    var status: ManualDwellOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDwellOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDwellOverrideStatus): Boolean {
        when (status) {
            ManualDwellOverrideStatus.IDLE -> return next == ManualDwellOverrideStatus.READY || next == ManualDwellOverrideStatus.DRAINING
            ManualDwellOverrideStatus.WARMING -> return next == ManualDwellOverrideStatus.READY
            ManualDwellOverrideStatus.READY -> return next == ManualDwellOverrideStatus.DRAINING || next == ManualDwellOverrideStatus.STOPPED
            ManualDwellOverrideStatus.DRAINING -> return next == ManualDwellOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualDwellOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual dwell override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((floorValue + discount) * 100) / 100

    fun snapshot(): ManualDwellOverride {
        return ManualDwellOverride(
            code = code,
            name = name,
            limit = limit,
            floorValue = floorValue,
            discount = discount,
            pinned = pinned,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual dwell override in status idle. */
fun newManualDwellOverride(code: String, name: String): ManualDwellOverride {
    return ManualDwellOverride(
        code = code,
        name = name,
        limit = 0,
        floorValue = 0.0,
        discount = 0.0,
        pinned = false,
        maximum = 0.0,
        status = ManualDwellOverrideStatus.IDLE,
    )
}
