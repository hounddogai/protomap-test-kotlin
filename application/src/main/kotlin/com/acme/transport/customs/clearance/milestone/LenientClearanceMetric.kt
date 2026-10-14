package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient clearance metric is in its life cycle. */
enum class LenientClearanceMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient clearance metric as the clearance team keeps it. */
data class LenientClearanceMetric(
    var code: String,
    var title: String,
    var minimum: Double,
    var amount: Double,
    var country: String,
    var shiftCode: String,
    var createdAt: Int,
    var timezone: String,
    var locale: String,
    var ownerId: String,
    var archived: Boolean,
    var status: LenientClearanceMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientClearanceMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientClearanceMetricStatus): Boolean {
        when (status) {
            LenientClearanceMetricStatus.PLANNED -> {
                when (next) {
                    LenientClearanceMetricStatus.SCHEDULED, LenientClearanceMetricStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientClearanceMetricStatus.SCHEDULED -> {
                when (next) {
                    LenientClearanceMetricStatus.IN_PROGRESS, LenientClearanceMetricStatus.COMPLETED -> return true
                }
                return false
            }
            LenientClearanceMetricStatus.IN_PROGRESS -> {
                when (next) {
                    LenientClearanceMetricStatus.COMPLETED -> return true
                }
                return false
            }
            LenientClearanceMetricStatus.COMPLETED -> {
                when (next) {
                    LenientClearanceMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientClearanceMetricStatus) {
        if (!canMoveTo(next)) {
            error("lenient clearance metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient clearance metric"} $code"

    fun projectedLoad(): Double = round(createdAt.toDouble() * minimum * 100) / 100

    fun snapshot(): LenientClearanceMetric {
        return LenientClearanceMetric(
            code = code,
            title = title,
            minimum = minimum,
            amount = amount,
            country = country,
            shiftCode = shiftCode,
            createdAt = createdAt,
            timezone = timezone,
            locale = locale,
            ownerId = ownerId,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient clearance metric in status planned. */
fun newLenientClearanceMetric(code: String, title: String): LenientClearanceMetric {
    return LenientClearanceMetric(
        code = code,
        title = title,
        minimum = 0.0,
        amount = 0.0,
        country = "",
        shiftCode = "",
        createdAt = 0,
        timezone = "",
        locale = "",
        ownerId = "",
        archived = false,
        status = LenientClearanceMetricStatus.PLANNED,
    )
}
