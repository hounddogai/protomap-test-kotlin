package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global backhaul threshold records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a global backhaul threshold is in its life cycle. */
enum class GlobalBackhaulThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global backhaul threshold and its current status. */
data class GlobalBackhaulThreshold(
    var code: String,
    var name: String,
    var odometer: Int,
    var visible: Boolean,
    var category: String,
    var distanceKm: Double,
    var createdAt: Int,
    var status: GlobalBackhaulThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBackhaulThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBackhaulThresholdStatus): Boolean {
        when (status) {
            GlobalBackhaulThresholdStatus.PROPOSED -> {
                when (next) {
                    GlobalBackhaulThresholdStatus.REJECTED -> return true
                }
                return false
            }
            GlobalBackhaulThresholdStatus.APPROVED -> return next == GlobalBackhaulThresholdStatus.WITHDRAWN
            GlobalBackhaulThresholdStatus.REJECTED -> {
                when (next) {
                    GlobalBackhaulThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalBackhaulThresholdStatus) {
        if (!canMoveTo(next)) {
            error("global backhaul threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((distanceKm - odometer.toDouble()) * 100) / 100

    fun snapshot(): GlobalBackhaulThreshold {
        return GlobalBackhaulThreshold(
            code = code,
            name = name,
            odometer = odometer,
            visible = visible,
            category = category,
            distanceKm = distanceKm,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global backhaul threshold in status proposed. */
fun newGlobalBackhaulThreshold(code: String, name: String): GlobalBackhaulThreshold {
    return GlobalBackhaulThreshold(
        code = code,
        name = name,
        odometer = 0,
        visible = false,
        category = "",
        distanceKm = 0.0,
        createdAt = 0,
        status = GlobalBackhaulThresholdStatus.PROPOSED,
    )
}
