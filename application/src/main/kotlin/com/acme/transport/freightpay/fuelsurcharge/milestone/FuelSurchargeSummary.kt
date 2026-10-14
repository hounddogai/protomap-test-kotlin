package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge summary is in its life cycle. */
enum class FuelSurchargeSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fuel surcharge summary. Its code is unique within a tenant. */
data class FuelSurchargeSummary(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var threshold: Double,
    var priority: Int,
    var maximum: Double,
    var vehicleId: String,
    var summary: String,
    var status: FuelSurchargeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeSummaryStatus): Boolean {
        when (status) {
            FuelSurchargeSummaryStatus.DRAFT -> {
                when (next) {
                    FuelSurchargeSummaryStatus.PENDING, FuelSurchargeSummaryStatus.ACTIVE -> return true
                }
                return false
            }
            FuelSurchargeSummaryStatus.PENDING -> return next == FuelSurchargeSummaryStatus.ACTIVE || next == FuelSurchargeSummaryStatus.SUSPENDED
            FuelSurchargeSummaryStatus.ACTIVE -> {
                when (next) {
                    FuelSurchargeSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            FuelSurchargeSummaryStatus.SUSPENDED -> return next == FuelSurchargeSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fuel surcharge summary"} $code"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): FuelSurchargeSummary {
        return FuelSurchargeSummary(
            code = code,
            label = label,
            expiresAt = expiresAt,
            threshold = threshold,
            priority = priority,
            maximum = maximum,
            vehicleId = vehicleId,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge summary in status draft. */
fun newFuelSurchargeSummary(code: String, label: String): FuelSurchargeSummary {
    return FuelSurchargeSummary(
        code = code,
        label = label,
        expiresAt = 0,
        threshold = 0.0,
        priority = 0,
        maximum = 0.0,
        vehicleId = "",
        summary = "",
        status = FuelSurchargeSummaryStatus.DRAFT,
    )
}
