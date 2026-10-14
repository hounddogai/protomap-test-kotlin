package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub estimate is in its life cycle. */
enum class HubEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One hub estimate, with what the planners need to act on it. */
data class HubEstimate(
    var code: String,
    var label: String,
    var odometer: Int,
    var country: String,
    var currency: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var status: HubEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubEstimateStatus): Boolean {
        when (status) {
            HubEstimateStatus.NEW -> return next == HubEstimateStatus.QUEUED || next == HubEstimateStatus.FAILED
            HubEstimateStatus.QUEUED -> return next == HubEstimateStatus.DONE || next == HubEstimateStatus.FAILED
            HubEstimateStatus.RUNNING -> {
                when (next) {
                    HubEstimateStatus.DONE, HubEstimateStatus.FAILED -> return true
                }
                return false
            }
            HubEstimateStatus.DONE -> {
                when (next) {
                    HubEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubEstimateStatus) {
        if (!canMoveTo(next)) {
            error("hub estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round(odometer.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): HubEstimate {
        return HubEstimate(
            code = code,
            label = label,
            odometer = odometer,
            country = country,
            currency = currency,
            tags = tags,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a hub estimate in status new. */
fun newHubEstimate(code: String, label: String): HubEstimate {
    return HubEstimate(
        code = code,
        label = label,
        odometer = 0,
        country = "",
        currency = "",
        tags = mutableListOf<String>(),
        updatedAt = 0,
        status = HubEstimateStatus.NEW,
    )
}
