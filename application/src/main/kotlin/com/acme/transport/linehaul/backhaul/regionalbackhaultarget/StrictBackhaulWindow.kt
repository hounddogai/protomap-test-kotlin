package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict backhaul window is in its life cycle. */
enum class StrictBackhaulWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict backhaul window and its current status. */
data class StrictBackhaulWindow(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var visible: Boolean,
    var quantity: Int,
    var status: StrictBackhaulWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulWindowStatus): Boolean {
        when (status) {
            StrictBackhaulWindowStatus.DRAFT -> {
                when (next) {
                    StrictBackhaulWindowStatus.ACTIVE, StrictBackhaulWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictBackhaulWindowStatus.PENDING -> {
                when (next) {
                    StrictBackhaulWindowStatus.ACTIVE, StrictBackhaulWindowStatus.CLOSED -> return true
                }
                return false
            }
            StrictBackhaulWindowStatus.ACTIVE -> return next == StrictBackhaulWindowStatus.SUSPENDED || next == StrictBackhaulWindowStatus.CLOSED
            StrictBackhaulWindowStatus.SUSPENDED -> return next == StrictBackhaulWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictBackhaulWindowStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(cost * quantity.toDouble() * 100) / 100

    fun snapshot(): StrictBackhaulWindow {
        return StrictBackhaulWindow(
            code = code,
            title = title,
            attributes = attributes,
            cost = cost,
            visible = visible,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict backhaul window in status draft. */
fun newStrictBackhaulWindow(code: String, title: String): StrictBackhaulWindow {
    return StrictBackhaulWindow(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        visible = false,
        quantity = 0,
        status = StrictBackhaulWindowStatus.DRAFT,
    )
}
