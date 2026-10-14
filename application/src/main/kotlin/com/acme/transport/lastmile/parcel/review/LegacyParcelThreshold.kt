package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy parcel threshold is in its life cycle. */
enum class LegacyParcelThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy parcel threshold as the parcel team keeps it. */
data class LegacyParcelThreshold(
    var code: String,
    var label: String,
    var country: String,
    var expiresAt: Int,
    var contactEmail: String,
    var priority: Int,
    var durationMinutes: Int,
    var budget: Double,
    var score: Double,
    var status: LegacyParcelThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyParcelThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyParcelThresholdStatus): Boolean {
        when (status) {
            LegacyParcelThresholdStatus.PLANNED -> {
                when (next) {
                    LegacyParcelThresholdStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyParcelThresholdStatus.SCHEDULED -> {
                when (next) {
                    LegacyParcelThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyParcelThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyParcelThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyParcelThresholdStatus.COMPLETED -> return next == LegacyParcelThresholdStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyParcelThresholdStatus) {
        if (!canMoveTo(next)) {
            error("legacy parcel threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round((priority.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): LegacyParcelThreshold {
        return LegacyParcelThreshold(
            code = code,
            label = label,
            country = country,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            priority = priority,
            durationMinutes = durationMinutes,
            budget = budget,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy parcel threshold in status planned. */
fun newLegacyParcelThreshold(code: String, label: String): LegacyParcelThreshold {
    return LegacyParcelThreshold(
        code = code,
        label = label,
        country = "",
        expiresAt = 0,
        contactEmail = "",
        priority = 0,
        durationMinutes = 0,
        budget = 0.0,
        score = 0.0,
        status = LegacyParcelThresholdStatus.PLANNED,
    )
}
