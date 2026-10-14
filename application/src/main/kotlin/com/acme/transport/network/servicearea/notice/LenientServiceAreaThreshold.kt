package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service area threshold is in its life cycle. */
enum class LenientServiceAreaThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lenient service area threshold and its current status. */
data class LenientServiceAreaThreshold(
    var code: String,
    var label: String,
    var ceiling: Double,
    var region: String,
    var currency: String,
    var revision: Int,
    var odometer: Int,
    var status: LenientServiceAreaThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaThresholdStatus): Boolean {
        when (status) {
            LenientServiceAreaThresholdStatus.PROPOSED -> return next == LenientServiceAreaThresholdStatus.APPROVED
            LenientServiceAreaThresholdStatus.APPROVED -> {
                when (next) {
                    LenientServiceAreaThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientServiceAreaThresholdStatus.REJECTED -> {
                when (next) {
                    LenientServiceAreaThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaThresholdStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"lenient service area threshold"} $code"

    fun weightedValue(): Double = round(ceiling * revision.toDouble() * 100) / 100

    fun snapshot(): LenientServiceAreaThreshold {
        return LenientServiceAreaThreshold(
            code = code,
            label = label,
            ceiling = ceiling,
            region = region,
            currency = currency,
            revision = revision,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient service area threshold in status proposed. */
fun newLenientServiceAreaThreshold(code: String, label: String): LenientServiceAreaThreshold {
    return LenientServiceAreaThreshold(
        code = code,
        label = label,
        ceiling = 0.0,
        region = "",
        currency = "",
        revision = 0,
        odometer = 0,
        status = LenientServiceAreaThresholdStatus.PROPOSED,
    )
}
