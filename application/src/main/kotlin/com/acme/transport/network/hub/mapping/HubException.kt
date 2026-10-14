package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub exception is in its life cycle. */
enum class HubExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a hub exception and its current status. */
data class HubException(
    var code: String,
    var label: String,
    var rate: Double,
    var country: String,
    var windowDays: Int,
    var batchSize: Int,
    var title: String,
    var reviewer: String,
    var status: HubExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubExceptionStatus): Boolean {
        when (status) {
            HubExceptionStatus.OPEN -> return next == HubExceptionStatus.RESOLVED || next == HubExceptionStatus.REOPENED
            HubExceptionStatus.IN_REVIEW -> return next == HubExceptionStatus.REOPENED
            HubExceptionStatus.RESOLVED -> return next == HubExceptionStatus.ARCHIVED
            HubExceptionStatus.REOPENED -> {
                when (next) {
                    HubExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubExceptionStatus) {
        if (!canMoveTo(next)) {
            error("hub exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round((batchSize.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): HubException {
        return HubException(
            code = code,
            label = label,
            rate = rate,
            country = country,
            windowDays = windowDays,
            batchSize = batchSize,
            title = title,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a hub exception in status open. */
fun newHubException(code: String, label: String): HubException {
    return HubException(
        code = code,
        label = label,
        rate = 0.0,
        country = "",
        windowDays = 0,
        batchSize = 0,
        title = "",
        reviewer = "",
        status = HubExceptionStatus.OPEN,
    )
}
