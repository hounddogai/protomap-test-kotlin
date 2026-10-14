package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize

/** Where a weighted violation request is in its life cycle. */
enum class WeightedViolationRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a weighted violation request and its current status. */
data class WeightedViolationRequest(
    var code: String,
    var label: String,
    var vehicleId: String,
    var title: String,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var status: WeightedViolationRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedViolationRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedViolationRequestStatus): Boolean {
        when (status) {
            WeightedViolationRequestStatus.CREATED -> {
                when (next) {
                    WeightedViolationRequestStatus.VALIDATED -> return true
                }
                return false
            }
            WeightedViolationRequestStatus.VALIDATED -> {
                when (next) {
                    WeightedViolationRequestStatus.PUBLISHED, WeightedViolationRequestStatus.RETIRED -> return true
                }
                return false
            }
            WeightedViolationRequestStatus.PUBLISHED -> return next == WeightedViolationRequestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedViolationRequestStatus) {
        if (!canMoveTo(next)) {
            error("weighted violation request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): WeightedViolationRequest {
        return WeightedViolationRequest(
            code = code,
            label = label,
            vehicleId = vehicleId,
            title = title,
            odometer = odometer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted violation request in status created. */
fun newWeightedViolationRequest(code: String, label: String): WeightedViolationRequest {
    return WeightedViolationRequest(
        code = code,
        label = label,
        vehicleId = "",
        title = "",
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        status = WeightedViolationRequestStatus.CREATED,
    )
}
