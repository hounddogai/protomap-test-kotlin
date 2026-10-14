package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default transit time quota is in its life cycle. */
enum class DefaultTransitTimeQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default transit time quota, with what the planners need to act on it. */
data class DefaultTransitTimeQuota(
    var code: String,
    var name: String,
    var category: String,
    var floorValue: Double,
    var minimum: Double,
    var margin: Double,
    var budget: Double,
    var rate: Double,
    var country: String,
    var status: DefaultTransitTimeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimeQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimeQuotaStatus): Boolean {
        when (status) {
            DefaultTransitTimeQuotaStatus.NEW -> {
                when (next) {
                    DefaultTransitTimeQuotaStatus.QUEUED, DefaultTransitTimeQuotaStatus.RUNNING -> return true
                }
                return false
            }
            DefaultTransitTimeQuotaStatus.QUEUED -> return next == DefaultTransitTimeQuotaStatus.DONE
            DefaultTransitTimeQuotaStatus.RUNNING -> return next == DefaultTransitTimeQuotaStatus.DONE || next == DefaultTransitTimeQuotaStatus.FAILED
            DefaultTransitTimeQuotaStatus.DONE -> return next == DefaultTransitTimeQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("default transit time quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((margin + floorValue) * 100) / 100

    fun snapshot(): DefaultTransitTimeQuota {
        return DefaultTransitTimeQuota(
            code = code,
            name = name,
            category = category,
            floorValue = floorValue,
            minimum = minimum,
            margin = margin,
            budget = budget,
            rate = rate,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default transit time quota in status new. */
fun newDefaultTransitTimeQuota(code: String, name: String): DefaultTransitTimeQuota {
    return DefaultTransitTimeQuota(
        code = code,
        name = name,
        category = "",
        floorValue = 0.0,
        minimum = 0.0,
        margin = 0.0,
        budget = 0.0,
        rate = 0.0,
        country = "",
        status = DefaultTransitTimeQuotaStatus.NEW,
    )
}
