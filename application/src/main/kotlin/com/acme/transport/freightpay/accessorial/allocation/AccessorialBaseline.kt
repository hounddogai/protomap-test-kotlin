package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial baseline is in its life cycle. */
enum class AccessorialBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One accessorial baseline, with what the planners need to act on it. */
data class AccessorialBaseline(
    var code: String,
    var title: String,
    var country: String,
    var name: String,
    var depot: String,
    var locale: String,
    var odometer: Int,
    var revision: Int,
    var externalRef: String,
    var status: AccessorialBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialBaselineStatus): Boolean {
        when (status) {
            AccessorialBaselineStatus.PLANNED -> return next == AccessorialBaselineStatus.SCHEDULED || next == AccessorialBaselineStatus.CANCELLED
            AccessorialBaselineStatus.SCHEDULED -> {
                when (next) {
                    AccessorialBaselineStatus.IN_PROGRESS, AccessorialBaselineStatus.COMPLETED -> return true
                }
                return false
            }
            AccessorialBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    AccessorialBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            AccessorialBaselineStatus.COMPLETED -> {
                when (next) {
                    AccessorialBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialBaselineStatus) {
        if (!canMoveTo(next)) {
            error("accessorial baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"accessorial baseline"} $code"

    fun netLoad(): Double = round((odometer.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): AccessorialBaseline {
        return AccessorialBaseline(
            code = code,
            title = title,
            country = country,
            name = name,
            depot = depot,
            locale = locale,
            odometer = odometer,
            revision = revision,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a accessorial baseline in status planned. */
fun newAccessorialBaseline(code: String, title: String): AccessorialBaseline {
    return AccessorialBaseline(
        code = code,
        title = title,
        country = "",
        name = "",
        depot = "",
        locale = "",
        odometer = 0,
        revision = 0,
        externalRef = "",
        status = AccessorialBaselineStatus.PLANNED,
    )
}
