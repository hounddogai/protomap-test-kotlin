package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge milestone is in its life cycle. */
enum class FuelSurchargeMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fuel surcharge milestone as the fuel surcharge team keeps it. */
data class FuelSurchargeMilestone(
    var code: String,
    var label: String,
    var checksum: String,
    var currency: String,
    var active: Boolean,
    var endsAt: Int,
    var version: Int,
    var retries: Int,
    var minimum: Double,
    var status: FuelSurchargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeMilestoneStatus): Boolean {
        when (status) {
            FuelSurchargeMilestoneStatus.IDLE -> return next == FuelSurchargeMilestoneStatus.WARMING || next == FuelSurchargeMilestoneStatus.READY
            FuelSurchargeMilestoneStatus.WARMING -> {
                when (next) {
                    FuelSurchargeMilestoneStatus.READY -> return true
                }
                return false
            }
            FuelSurchargeMilestoneStatus.READY -> {
                when (next) {
                    FuelSurchargeMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            FuelSurchargeMilestoneStatus.DRAINING -> return next == FuelSurchargeMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"fuel surcharge milestone"} $code"

    fun projectedExposure(): Double = round(retries.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): FuelSurchargeMilestone {
        return FuelSurchargeMilestone(
            code = code,
            label = label,
            checksum = checksum,
            currency = currency,
            active = active,
            endsAt = endsAt,
            version = version,
            retries = retries,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge milestone in status idle. */
fun newFuelSurchargeMilestone(code: String, label: String): FuelSurchargeMilestone {
    return FuelSurchargeMilestone(
        code = code,
        label = label,
        checksum = "",
        currency = "",
        active = false,
        endsAt = 0,
        version = 0,
        retries = 0,
        minimum = 0.0,
        status = FuelSurchargeMilestoneStatus.IDLE,
    )
}
