package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial template is in its life cycle. */
enum class AccessorialTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a accessorial template and its current status. */
data class AccessorialTemplate(
    var code: String,
    var name: String,
    var externalRef: String,
    var channel: String,
    var expiresAt: Int,
    var odometer: Int,
    var carrier: String,
    var driverId: String,
    var status: AccessorialTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialTemplateStatus): Boolean {
        when (status) {
            AccessorialTemplateStatus.OPEN -> {
                when (next) {
                    AccessorialTemplateStatus.REOPENED -> return true
                }
                return false
            }
            AccessorialTemplateStatus.IN_REVIEW -> {
                when (next) {
                    AccessorialTemplateStatus.REOPENED -> return true
                }
                return false
            }
            AccessorialTemplateStatus.RESOLVED -> {
                when (next) {
                    AccessorialTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialTemplateStatus.REOPENED -> {
                when (next) {
                    AccessorialTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialTemplateStatus) {
        if (!canMoveTo(next)) {
            error("accessorial template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"accessorial template"} $code"

    fun effectiveTotal(): Double = round(expiresAt.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): AccessorialTemplate {
        return AccessorialTemplate(
            code = code,
            name = name,
            externalRef = externalRef,
            channel = channel,
            expiresAt = expiresAt,
            odometer = odometer,
            carrier = carrier,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a accessorial template in status open. */
fun newAccessorialTemplate(code: String, name: String): AccessorialTemplate {
    return AccessorialTemplate(
        code = code,
        name = name,
        externalRef = "",
        channel = "",
        expiresAt = 0,
        odometer = 0,
        carrier = "",
        driverId = "",
        status = AccessorialTemplateStatus.OPEN,
    )
}
