package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.text.normalize

/** Where a default dwell exception is in its life cycle. */
enum class DefaultDwellExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default dwell exception as the dwell team keeps it. */
data class DefaultDwellException(
    var code: String,
    var title: String,
    var reviewer: String,
    var phone: String,
    var distanceKm: Double,
    var status: DefaultDwellExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDwellExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDwellExceptionStatus): Boolean {
        when (status) {
            DefaultDwellExceptionStatus.REQUESTED -> {
                when (next) {
                    DefaultDwellExceptionStatus.GRANTED, DefaultDwellExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultDwellExceptionStatus.GRANTED -> return next == DefaultDwellExceptionStatus.REVOKED
            DefaultDwellExceptionStatus.REVOKED -> {
                when (next) {
                    DefaultDwellExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDwellExceptionStatus) {
        if (!canMoveTo(next)) {
            error("default dwell exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): DefaultDwellException {
        return DefaultDwellException(
            code = code,
            title = title,
            reviewer = reviewer,
            phone = phone,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default dwell exception in status requested. */
fun newDefaultDwellException(code: String, title: String): DefaultDwellException {
    return DefaultDwellException(
        code = code,
        title = title,
        reviewer = "",
        phone = "",
        distanceKm = 0.0,
        status = DefaultDwellExceptionStatus.REQUESTED,
    )
}
