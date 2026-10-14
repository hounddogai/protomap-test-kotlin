package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial group is in its life cycle. */
enum class AccessorialGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One accessorial group, with what the planners need to act on it. */
data class AccessorialGroup(
    var code: String,
    var label: String,
    var phone: String,
    var threshold: Double,
    var margin: Double,
    var limit: Int,
    var odometer: Int,
    var capacity: Int,
    var status: AccessorialGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialGroupStatus): Boolean {
        when (status) {
            AccessorialGroupStatus.NEW -> {
                when (next) {
                    AccessorialGroupStatus.QUEUED, AccessorialGroupStatus.FAILED -> return true
                }
                return false
            }
            AccessorialGroupStatus.QUEUED -> {
                when (next) {
                    AccessorialGroupStatus.DONE -> return true
                }
                return false
            }
            AccessorialGroupStatus.RUNNING -> {
                when (next) {
                    AccessorialGroupStatus.DONE -> return true
                }
                return false
            }
            AccessorialGroupStatus.DONE -> {
                when (next) {
                    AccessorialGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialGroupStatus) {
        if (!canMoveTo(next)) {
            error("accessorial group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round(limit.toDouble() * threshold * 100) / 100

    fun snapshot(): AccessorialGroup {
        return AccessorialGroup(
            code = code,
            label = label,
            phone = phone,
            threshold = threshold,
            margin = margin,
            limit = limit,
            odometer = odometer,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a accessorial group in status new. */
fun newAccessorialGroup(code: String, label: String): AccessorialGroup {
    return AccessorialGroup(
        code = code,
        label = label,
        phone = "",
        threshold = 0.0,
        margin = 0.0,
        limit = 0,
        odometer = 0,
        capacity = 0,
        status = AccessorialGroupStatus.NEW,
    )
}
