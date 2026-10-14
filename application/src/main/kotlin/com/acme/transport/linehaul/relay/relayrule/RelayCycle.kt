package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay cycle is in its life cycle. */
enum class RelayCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A relay cycle as the relay team keeps it. */
data class RelayCycle(
    var code: String,
    var label: String,
    var maximum: Double,
    var rate: Double,
    var phone: String,
    var visible: Boolean,
    var status: RelayCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayCycleStatus): Boolean {
        when (status) {
            RelayCycleStatus.PLANNED -> return next == RelayCycleStatus.SCHEDULED || next == RelayCycleStatus.IN_PROGRESS
            RelayCycleStatus.SCHEDULED -> return next == RelayCycleStatus.CANCELLED
            RelayCycleStatus.IN_PROGRESS -> {
                when (next) {
                    RelayCycleStatus.CANCELLED -> return true
                }
                return false
            }
            RelayCycleStatus.COMPLETED -> return next == RelayCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RelayCycleStatus) {
        if (!canMoveTo(next)) {
            error("relay cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((rate - maximum) * 100) / 100

    fun snapshot(): RelayCycle {
        return RelayCycle(
            code = code,
            label = label,
            maximum = maximum,
            rate = rate,
            phone = phone,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a relay cycle in status planned. */
fun newRelayCycle(code: String, label: String): RelayCycle {
    return RelayCycle(
        code = code,
        label = label,
        maximum = 0.0,
        rate = 0.0,
        phone = "",
        visible = false,
        status = RelayCycleStatus.PLANNED,
    )
}
