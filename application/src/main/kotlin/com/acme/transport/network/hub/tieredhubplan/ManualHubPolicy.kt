package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hub policy is in its life cycle. */
enum class ManualHubPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual hub policy, with what the planners need to act on it. */
data class ManualHubPolicy(
    var code: String,
    var name: String,
    var routeCode: String,
    var dock: String,
    var endsAt: Int,
    var currency: String,
    var threshold: Double,
    var windowDays: Int,
    var status: ManualHubPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHubPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHubPolicyStatus): Boolean {
        when (status) {
            ManualHubPolicyStatus.DRAFT -> {
                when (next) {
                    ManualHubPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualHubPolicyStatus.PENDING -> {
                when (next) {
                    ManualHubPolicyStatus.ACTIVE -> return true
                }
                return false
            }
            ManualHubPolicyStatus.ACTIVE -> return next == ManualHubPolicyStatus.SUSPENDED || next == ManualHubPolicyStatus.CLOSED
            ManualHubPolicyStatus.SUSPENDED -> {
                when (next) {
                    ManualHubPolicyStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualHubPolicyStatus) {
        if (!canMoveTo(next)) {
            error("manual hub policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ManualHubPolicy {
        return ManualHubPolicy(
            code = code,
            name = name,
            routeCode = routeCode,
            dock = dock,
            endsAt = endsAt,
            currency = currency,
            threshold = threshold,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual hub policy in status draft. */
fun newManualHubPolicy(code: String, name: String): ManualHubPolicy {
    return ManualHubPolicy(
        code = code,
        name = name,
        routeCode = "",
        dock = "",
        endsAt = 0,
        currency = "",
        threshold = 0.0,
        windowDays = 0,
        status = ManualHubPolicyStatus.DRAFT,
    )
}
