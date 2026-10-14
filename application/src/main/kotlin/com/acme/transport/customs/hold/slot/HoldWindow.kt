package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold window is in its life cycle. */
enum class HoldWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hold window. Its code is unique within a tenant. */
data class HoldWindow(
    var code: String,
    var name: String,
    var source: String,
    var archived: Boolean,
    var notes: String,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var windowDays: Int,
    var loadKg: Double,
    var status: HoldWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldWindowStatus): Boolean {
        when (status) {
            HoldWindowStatus.CREATED -> {
                when (next) {
                    HoldWindowStatus.PUBLISHED -> return true
                }
                return false
            }
            HoldWindowStatus.VALIDATED -> return next == HoldWindowStatus.RETIRED
            HoldWindowStatus.PUBLISHED -> return next == HoldWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HoldWindowStatus) {
        if (!canMoveTo(next)) {
            error("hold window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(odometer.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): HoldWindow {
        return HoldWindow(
            code = code,
            name = name,
            source = source,
            archived = archived,
            notes = notes,
            odometer = odometer,
            attributes = attributes,
            windowDays = windowDays,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hold window in status created. */
fun newHoldWindow(code: String, name: String): HoldWindow {
    return HoldWindow(
        code = code,
        name = name,
        source = "",
        archived = false,
        notes = "",
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        windowDays = 0,
        loadKg = 0.0,
        status = HoldWindowStatus.CREATED,
    )
}
