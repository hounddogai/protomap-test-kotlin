package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default hub estimate records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a default hub estimate is in its life cycle. */
enum class DefaultHubEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default hub estimate and its current status. */
data class DefaultHubEstimate(
    var code: String,
    var label: String,
    var budget: Double,
    var country: String,
    var maximum: Double,
    var status: DefaultHubEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubEstimateStatus): Boolean {
        when (status) {
            DefaultHubEstimateStatus.NEW -> {
                when (next) {
                    DefaultHubEstimateStatus.DONE, DefaultHubEstimateStatus.FAILED -> return true
                }
                return false
            }
            DefaultHubEstimateStatus.QUEUED -> {
                when (next) {
                    DefaultHubEstimateStatus.RUNNING, DefaultHubEstimateStatus.DONE -> return true
                }
                return false
            }
            DefaultHubEstimateStatus.RUNNING -> {
                when (next) {
                    DefaultHubEstimateStatus.DONE, DefaultHubEstimateStatus.FAILED -> return true
                }
                return false
            }
            DefaultHubEstimateStatus.DONE -> return next == DefaultHubEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultHubEstimateStatus) {
        if (!canMoveTo(next)) {
            error("default hub estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default hub estimate"} $code"

    fun weightedTotal(): Double = round((budget - maximum) * 100) / 100

    fun snapshot(): DefaultHubEstimate {
        return DefaultHubEstimate(
            code = code,
            label = label,
            budget = budget,
            country = country,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default hub estimate in status new. */
fun newDefaultHubEstimate(code: String, label: String): DefaultHubEstimate {
    return DefaultHubEstimate(
        code = code,
        label = label,
        budget = 0.0,
        country = "",
        maximum = 0.0,
        status = DefaultHubEstimateStatus.NEW,
    )
}
