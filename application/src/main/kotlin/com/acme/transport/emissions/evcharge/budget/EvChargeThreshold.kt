package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge threshold is in its life cycle. */
enum class EvChargeThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One ev charge threshold, with what the planners need to act on it. */
data class EvChargeThreshold(
    var code: String,
    var label: String,
    var odometer: Int,
    var floorValue: Double,
    var retries: Int,
    var updatedAt: Int,
    var endsAt: Int,
    var description: String,
    var attributes: MutableMap<String, String>,
    var status: EvChargeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeThresholdStatus): Boolean {
        when (status) {
            EvChargeThresholdStatus.CREATED -> {
                when (next) {
                    EvChargeThresholdStatus.PUBLISHED -> return true
                }
                return false
            }
            EvChargeThresholdStatus.VALIDATED -> return next == EvChargeThresholdStatus.RETIRED
            EvChargeThresholdStatus.PUBLISHED -> {
                when (next) {
                    EvChargeThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("ev charge threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(odometer.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): EvChargeThreshold {
        return EvChargeThreshold(
            code = code,
            label = label,
            odometer = odometer,
            floorValue = floorValue,
            retries = retries,
            updatedAt = updatedAt,
            endsAt = endsAt,
            description = description,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a ev charge threshold in status created. */
fun newEvChargeThreshold(code: String, label: String): EvChargeThreshold {
    return EvChargeThreshold(
        code = code,
        label = label,
        odometer = 0,
        floorValue = 0.0,
        retries = 0,
        updatedAt = 0,
        endsAt = 0,
        description = "",
        attributes = mutableMapOf<String, String>(),
        status = EvChargeThresholdStatus.CREATED,
    )
}
