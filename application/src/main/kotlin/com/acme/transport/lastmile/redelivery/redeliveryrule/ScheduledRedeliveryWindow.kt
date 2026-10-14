package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled redelivery window is in its life cycle. */
enum class ScheduledRedeliveryWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled redelivery window. Its code is unique within a tenant. */
data class ScheduledRedeliveryWindow(
    var code: String,
    var name: String,
    var weight: Double,
    var label: String,
    var pallets: Int,
    var durationMinutes: Int,
    var etaMinutes: Int,
    var minimum: Double,
    var status: ScheduledRedeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryWindowStatus): Boolean {
        when (status) {
            ScheduledRedeliveryWindowStatus.DRAFT -> {
                when (next) {
                    ScheduledRedeliveryWindowStatus.SUSPENDED, ScheduledRedeliveryWindowStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRedeliveryWindowStatus.PENDING -> {
                when (next) {
                    ScheduledRedeliveryWindowStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRedeliveryWindowStatus.ACTIVE -> {
                when (next) {
                    ScheduledRedeliveryWindowStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRedeliveryWindowStatus.SUSPENDED -> {
                when (next) {
                    ScheduledRedeliveryWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun netLoad(): Double = round((etaMinutes.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRedeliveryWindow {
        return ScheduledRedeliveryWindow(
            code = code,
            name = name,
            weight = weight,
            label = label,
            pallets = pallets,
            durationMinutes = durationMinutes,
            etaMinutes = etaMinutes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled redelivery window in status draft. */
fun newScheduledRedeliveryWindow(code: String, name: String): ScheduledRedeliveryWindow {
    return ScheduledRedeliveryWindow(
        code = code,
        name = name,
        weight = 0.0,
        label = "",
        pallets = 0,
        durationMinutes = 0,
        etaMinutes = 0,
        minimum = 0.0,
        status = ScheduledRedeliveryWindowStatus.DRAFT,
    )
}
