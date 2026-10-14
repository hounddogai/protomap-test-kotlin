package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge adjustment is in its life cycle. */
enum class EvChargeAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A ev charge adjustment. Its code is unique within a tenant. */
data class EvChargeAdjustment(
    var code: String,
    var title: String,
    var reviewer: String,
    var version: Int,
    var checksum: String,
    var fuelLiters: Double,
    var status: EvChargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeAdjustmentStatus): Boolean {
        when (status) {
            EvChargeAdjustmentStatus.NEW -> {
                when (next) {
                    EvChargeAdjustmentStatus.QUEUED -> return true
                }
                return false
            }
            EvChargeAdjustmentStatus.QUEUED -> {
                when (next) {
                    EvChargeAdjustmentStatus.RUNNING -> return true
                }
                return false
            }
            EvChargeAdjustmentStatus.RUNNING -> return next == EvChargeAdjustmentStatus.DONE
            EvChargeAdjustmentStatus.DONE -> {
                when (next) {
                    EvChargeAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("ev charge adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"ev charge adjustment"} $code"

    fun adjustedExposure(): Double = round((fuelLiters - version.toDouble()) * 100) / 100

    fun snapshot(): EvChargeAdjustment {
        return EvChargeAdjustment(
            code = code,
            title = title,
            reviewer = reviewer,
            version = version,
            checksum = checksum,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a ev charge adjustment in status new. */
fun newEvChargeAdjustment(code: String, title: String): EvChargeAdjustment {
    return EvChargeAdjustment(
        code = code,
        title = title,
        reviewer = "",
        version = 0,
        checksum = "",
        fuelLiters = 0.0,
        status = EvChargeAdjustmentStatus.NEW,
    )
}
