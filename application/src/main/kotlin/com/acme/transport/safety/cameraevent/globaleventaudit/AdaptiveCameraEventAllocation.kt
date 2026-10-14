package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive camera event allocation is in its life cycle. */
enum class AdaptiveCameraEventAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive camera event allocation as the camera event team keeps it. */
data class AdaptiveCameraEventAllocation(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var retries: Int,
    var loadKg: Double,
    var version: Int,
    var status: AdaptiveCameraEventAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCameraEventAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCameraEventAllocationStatus): Boolean {
        when (status) {
            AdaptiveCameraEventAllocationStatus.REQUESTED -> {
                when (next) {
                    AdaptiveCameraEventAllocationStatus.GRANTED, AdaptiveCameraEventAllocationStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveCameraEventAllocationStatus.GRANTED -> {
                when (next) {
                    AdaptiveCameraEventAllocationStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveCameraEventAllocationStatus.REVOKED -> return next == AdaptiveCameraEventAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveCameraEventAllocationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive camera event allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round(durationMinutes.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): AdaptiveCameraEventAllocation {
        return AdaptiveCameraEventAllocation(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            retries = retries,
            loadKg = loadKg,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive camera event allocation in status requested. */
fun newAdaptiveCameraEventAllocation(code: String, label: String): AdaptiveCameraEventAllocation {
    return AdaptiveCameraEventAllocation(
        code = code,
        label = label,
        durationMinutes = 0,
        retries = 0,
        loadKg = 0.0,
        version = 0,
        status = AdaptiveCameraEventAllocationStatus.REQUESTED,
    )
}
