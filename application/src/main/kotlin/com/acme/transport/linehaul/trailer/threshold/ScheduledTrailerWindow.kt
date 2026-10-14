package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled trailer window is in its life cycle. */
enum class ScheduledTrailerWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled trailer window. Its code is unique within a tenant. */
data class ScheduledTrailerWindow(
    var code: String,
    var title: String,
    var region: String,
    var odometer: Int,
    var quantity: Int,
    var status: ScheduledTrailerWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerWindowStatus): Boolean {
        when (status) {
            ScheduledTrailerWindowStatus.CREATED -> {
                when (next) {
                    ScheduledTrailerWindowStatus.PUBLISHED, ScheduledTrailerWindowStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledTrailerWindowStatus.VALIDATED -> {
                when (next) {
                    ScheduledTrailerWindowStatus.PUBLISHED, ScheduledTrailerWindowStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledTrailerWindowStatus.PUBLISHED -> {
                when (next) {
                    ScheduledTrailerWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerWindowStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((odometer.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledTrailerWindow {
        return ScheduledTrailerWindow(
            code = code,
            title = title,
            region = region,
            odometer = odometer,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled trailer window in status created. */
fun newScheduledTrailerWindow(code: String, title: String): ScheduledTrailerWindow {
    return ScheduledTrailerWindow(
        code = code,
        title = title,
        region = "",
        odometer = 0,
        quantity = 0,
        status = ScheduledTrailerWindowStatus.CREATED,
    )
}
