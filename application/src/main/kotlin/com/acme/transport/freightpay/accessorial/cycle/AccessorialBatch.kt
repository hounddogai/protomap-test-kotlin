package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial batch is in its life cycle. */
enum class AccessorialBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One accessorial batch, with what the planners need to act on it. */
data class AccessorialBatch(
    var code: String,
    var label: String,
    var floorValue: Double,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var status: AccessorialBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialBatchStatus): Boolean {
        when (status) {
            AccessorialBatchStatus.OPEN -> {
                when (next) {
                    AccessorialBatchStatus.RESOLVED -> return true
                }
                return false
            }
            AccessorialBatchStatus.IN_REVIEW -> return next == AccessorialBatchStatus.REOPENED || next == AccessorialBatchStatus.ARCHIVED
            AccessorialBatchStatus.RESOLVED -> return next == AccessorialBatchStatus.REOPENED
            AccessorialBatchStatus.REOPENED -> {
                when (next) {
                    AccessorialBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialBatchStatus) {
        if (!canMoveTo(next)) {
            error("accessorial batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"accessorial batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((odometer.toDouble() + floorValue) * 100) / 100

    fun snapshot(): AccessorialBatch {
        return AccessorialBatch(
            code = code,
            label = label,
            floorValue = floorValue,
            odometer = odometer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a accessorial batch in status open. */
fun newAccessorialBatch(code: String, label: String): AccessorialBatch {
    return AccessorialBatch(
        code = code,
        label = label,
        floorValue = 0.0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        status = AccessorialBatchStatus.OPEN,
    )
}
