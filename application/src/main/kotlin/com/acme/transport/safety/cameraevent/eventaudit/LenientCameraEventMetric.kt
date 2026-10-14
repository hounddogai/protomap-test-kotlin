package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient camera event metric is in its life cycle. */
enum class LenientCameraEventMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient camera event metric as the camera event team keeps it. */
data class LenientCameraEventMetric(
    var code: String,
    var title: String,
    var shiftCode: String,
    var active: Boolean,
    var endsAt: Int,
    var country: String,
    var discount: Double,
    var driverId: String,
    var status: LenientCameraEventMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCameraEventMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCameraEventMetricStatus): Boolean {
        when (status) {
            LenientCameraEventMetricStatus.PLANNED -> return next == LenientCameraEventMetricStatus.SCHEDULED || next == LenientCameraEventMetricStatus.CANCELLED
            LenientCameraEventMetricStatus.SCHEDULED -> {
                when (next) {
                    LenientCameraEventMetricStatus.IN_PROGRESS, LenientCameraEventMetricStatus.COMPLETED -> return true
                }
                return false
            }
            LenientCameraEventMetricStatus.IN_PROGRESS -> return next == LenientCameraEventMetricStatus.COMPLETED || next == LenientCameraEventMetricStatus.CANCELLED
            LenientCameraEventMetricStatus.COMPLETED -> return next == LenientCameraEventMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientCameraEventMetricStatus) {
        if (!canMoveTo(next)) {
            error("lenient camera event metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((endsAt.toDouble() + discount) * 100) / 100

    fun snapshot(): LenientCameraEventMetric {
        return LenientCameraEventMetric(
            code = code,
            title = title,
            shiftCode = shiftCode,
            active = active,
            endsAt = endsAt,
            country = country,
            discount = discount,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient camera event metric in status planned. */
fun newLenientCameraEventMetric(code: String, title: String): LenientCameraEventMetric {
    return LenientCameraEventMetric(
        code = code,
        title = title,
        shiftCode = "",
        active = false,
        endsAt = 0,
        country = "",
        discount = 0.0,
        driverId = "",
        status = LenientCameraEventMetricStatus.PLANNED,
    )
}
