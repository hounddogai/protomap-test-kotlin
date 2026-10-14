package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped ev charge variance is in its life cycle. */
enum class CappedEvChargeVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped ev charge variance. Its code is unique within a tenant. */
data class CappedEvChargeVariance(
    var code: String,
    var label: String,
    var threshold: Double,
    var vehicleId: String,
    var ownerId: String,
    var pinned: Boolean,
    var graceDays: Int,
    var summary: String,
    var category: String,
    var pallets: Int,
    var status: CappedEvChargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedEvChargeVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedEvChargeVarianceStatus): Boolean {
        when (status) {
            CappedEvChargeVarianceStatus.NEW -> return next == CappedEvChargeVarianceStatus.QUEUED || next == CappedEvChargeVarianceStatus.FAILED
            CappedEvChargeVarianceStatus.QUEUED -> {
                when (next) {
                    CappedEvChargeVarianceStatus.DONE -> return true
                }
                return false
            }
            CappedEvChargeVarianceStatus.RUNNING -> return next == CappedEvChargeVarianceStatus.DONE || next == CappedEvChargeVarianceStatus.FAILED
            CappedEvChargeVarianceStatus.DONE -> return next == CappedEvChargeVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedEvChargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("capped ev charge variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((threshold - graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedEvChargeVariance {
        return CappedEvChargeVariance(
            code = code,
            label = label,
            threshold = threshold,
            vehicleId = vehicleId,
            ownerId = ownerId,
            pinned = pinned,
            graceDays = graceDays,
            summary = summary,
            category = category,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped ev charge variance in status new. */
fun newCappedEvChargeVariance(code: String, label: String): CappedEvChargeVariance {
    return CappedEvChargeVariance(
        code = code,
        label = label,
        threshold = 0.0,
        vehicleId = "",
        ownerId = "",
        pinned = false,
        graceDays = 0,
        summary = "",
        category = "",
        pallets = 0,
        status = CappedEvChargeVarianceStatus.NEW,
    )
}
