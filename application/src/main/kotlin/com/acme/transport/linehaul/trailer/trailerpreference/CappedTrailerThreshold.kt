package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer threshold is in its life cycle. */
enum class CappedTrailerThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped trailer threshold. Its code is unique within a tenant. */
data class CappedTrailerThreshold(
    var code: String,
    var label: String,
    var phone: String,
    var attempts: Int,
    var graceDays: Int,
    var vehicleId: String,
    var externalRef: String,
    var status: CappedTrailerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerThresholdStatus): Boolean {
        when (status) {
            CappedTrailerThresholdStatus.PROPOSED -> {
                when (next) {
                    CappedTrailerThresholdStatus.APPROVED, CappedTrailerThresholdStatus.REJECTED -> return true
                }
                return false
            }
            CappedTrailerThresholdStatus.APPROVED -> {
                when (next) {
                    CappedTrailerThresholdStatus.REJECTED, CappedTrailerThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedTrailerThresholdStatus.REJECTED -> {
                when (next) {
                    CappedTrailerThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round(attempts.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): CappedTrailerThreshold {
        return CappedTrailerThreshold(
            code = code,
            label = label,
            phone = phone,
            attempts = attempts,
            graceDays = graceDays,
            vehicleId = vehicleId,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped trailer threshold in status proposed. */
fun newCappedTrailerThreshold(code: String, label: String): CappedTrailerThreshold {
    return CappedTrailerThreshold(
        code = code,
        label = label,
        phone = "",
        attempts = 0,
        graceDays = 0,
        vehicleId = "",
        externalRef = "",
        status = CappedTrailerThresholdStatus.PROPOSED,
    )
}
