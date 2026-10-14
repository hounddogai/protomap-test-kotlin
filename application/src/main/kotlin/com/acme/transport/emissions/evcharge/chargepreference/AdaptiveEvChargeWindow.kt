package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive ev charge window is in its life cycle. */
enum class AdaptiveEvChargeWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive ev charge window. Its code is unique within a tenant. */
data class AdaptiveEvChargeWindow(
    var code: String,
    var name: String,
    var margin: Double,
    var reviewer: String,
    var revision: Int,
    var status: AdaptiveEvChargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEvChargeWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEvChargeWindowStatus): Boolean {
        when (status) {
            AdaptiveEvChargeWindowStatus.IDLE -> return next == AdaptiveEvChargeWindowStatus.DRAINING
            AdaptiveEvChargeWindowStatus.WARMING -> {
                when (next) {
                    AdaptiveEvChargeWindowStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveEvChargeWindowStatus.READY -> return next == AdaptiveEvChargeWindowStatus.STOPPED
            AdaptiveEvChargeWindowStatus.DRAINING -> {
                when (next) {
                    AdaptiveEvChargeWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEvChargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive ev charge window $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive ev charge window"} $code"

    fun netValue(): Double = round((revision.toDouble() + margin) * 100) / 100

    fun snapshot(): AdaptiveEvChargeWindow {
        return AdaptiveEvChargeWindow(
            code = code,
            name = name,
            margin = margin,
            reviewer = reviewer,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive ev charge window in status idle. */
fun newAdaptiveEvChargeWindow(code: String, name: String): AdaptiveEvChargeWindow {
    return AdaptiveEvChargeWindow(
        code = code,
        name = name,
        margin = 0.0,
        reviewer = "",
        revision = 0,
        status = AdaptiveEvChargeWindowStatus.IDLE,
    )
}
