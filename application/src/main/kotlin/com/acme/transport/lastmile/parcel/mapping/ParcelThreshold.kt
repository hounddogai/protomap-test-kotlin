package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.normalize

/** Where a parcel threshold is in its life cycle. */
enum class ParcelThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A parcel threshold. Its code is unique within a tenant. */
data class ParcelThreshold(
    var code: String,
    var label: String,
    var description: String,
    var odometer: Int,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var shiftCode: String,
    var status: ParcelThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelThresholdStatus): Boolean {
        when (status) {
            ParcelThresholdStatus.IDLE -> {
                when (next) {
                    ParcelThresholdStatus.READY -> return true
                }
                return false
            }
            ParcelThresholdStatus.WARMING -> {
                when (next) {
                    ParcelThresholdStatus.READY, ParcelThresholdStatus.STOPPED -> return true
                }
                return false
            }
            ParcelThresholdStatus.READY -> {
                when (next) {
                    ParcelThresholdStatus.DRAINING -> return true
                }
                return false
            }
            ParcelThresholdStatus.DRAINING -> return next == ParcelThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ParcelThresholdStatus) {
        if (!canMoveTo(next)) {
            error("parcel threshold $code cannot move from $status to $next")
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

    fun snapshot(): ParcelThreshold {
        return ParcelThreshold(
            code = code,
            label = label,
            description = description,
            odometer = odometer,
            timezone = timezone,
            attributes = attributes,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel threshold in status idle. */
fun newParcelThreshold(code: String, label: String): ParcelThreshold {
    return ParcelThreshold(
        code = code,
        label = label,
        description = "",
        odometer = 0,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        shiftCode = "",
        status = ParcelThresholdStatus.IDLE,
    )
}
