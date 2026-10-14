package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard bundle is in its life cycle. */
enum class RollingTrailerYardBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling trailer yard bundle. Its code is unique within a tenant. */
data class RollingTrailerYardBundle(
    var code: String,
    var label: String,
    var shiftCode: String,
    var title: String,
    var odometer: Int,
    var stopCount: Int,
    var active: Boolean,
    var carrier: String,
    var status: RollingTrailerYardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardBundleStatus): Boolean {
        when (status) {
            RollingTrailerYardBundleStatus.IDLE -> {
                when (next) {
                    RollingTrailerYardBundleStatus.WARMING -> return true
                }
                return false
            }
            RollingTrailerYardBundleStatus.WARMING -> return next == RollingTrailerYardBundleStatus.STOPPED
            RollingTrailerYardBundleStatus.READY -> {
                when (next) {
                    RollingTrailerYardBundleStatus.DRAINING, RollingTrailerYardBundleStatus.STOPPED -> return true
                }
                return false
            }
            RollingTrailerYardBundleStatus.DRAINING -> {
                when (next) {
                    RollingTrailerYardBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardBundleStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling trailer yard bundle"} $code"

    fun effectiveExposure(): Double = round(odometer.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): RollingTrailerYardBundle {
        return RollingTrailerYardBundle(
            code = code,
            label = label,
            shiftCode = shiftCode,
            title = title,
            odometer = odometer,
            stopCount = stopCount,
            active = active,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling trailer yard bundle in status idle. */
fun newRollingTrailerYardBundle(code: String, label: String): RollingTrailerYardBundle {
    return RollingTrailerYardBundle(
        code = code,
        label = label,
        shiftCode = "",
        title = "",
        odometer = 0,
        stopCount = 0,
        active = false,
        carrier = "",
        status = RollingTrailerYardBundleStatus.IDLE,
    )
}
