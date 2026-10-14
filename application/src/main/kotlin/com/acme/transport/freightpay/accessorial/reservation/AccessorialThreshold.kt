package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial threshold is in its life cycle. */
enum class AccessorialThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a accessorial threshold and its current status. */
data class AccessorialThreshold(
    var code: String,
    var name: String,
    var contactEmail: String,
    var routeCode: String,
    var expiresAt: Int,
    var label: String,
    var cost: Double,
    var minimum: Double,
    var archived: Boolean,
    var batchSize: Int,
    var status: AccessorialThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialThresholdStatus): Boolean {
        when (status) {
            AccessorialThresholdStatus.PLANNED -> {
                when (next) {
                    AccessorialThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            AccessorialThresholdStatus.SCHEDULED -> {
                when (next) {
                    AccessorialThresholdStatus.IN_PROGRESS, AccessorialThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            AccessorialThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    AccessorialThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            AccessorialThresholdStatus.COMPLETED -> {
                when (next) {
                    AccessorialThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialThresholdStatus) {
        if (!canMoveTo(next)) {
            error("accessorial threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"accessorial threshold"} $code"

    fun netExposure(): Double = round((expiresAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): AccessorialThreshold {
        return AccessorialThreshold(
            code = code,
            name = name,
            contactEmail = contactEmail,
            routeCode = routeCode,
            expiresAt = expiresAt,
            label = label,
            cost = cost,
            minimum = minimum,
            archived = archived,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a accessorial threshold in status planned. */
fun newAccessorialThreshold(code: String, name: String): AccessorialThreshold {
    return AccessorialThreshold(
        code = code,
        name = name,
        contactEmail = "",
        routeCode = "",
        expiresAt = 0,
        label = "",
        cost = 0.0,
        minimum = 0.0,
        archived = false,
        batchSize = 0,
        status = AccessorialThresholdStatus.PLANNED,
    )
}
