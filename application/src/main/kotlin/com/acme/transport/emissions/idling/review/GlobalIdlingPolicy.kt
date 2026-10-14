package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global idling policy is in its life cycle. */
enum class GlobalIdlingPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global idling policy. Its code is unique within a tenant. */
data class GlobalIdlingPolicy(
    var code: String,
    var title: String,
    var routeCode: String,
    var score: Double,
    var createdAt: Int,
    var windowDays: Int,
    var status: GlobalIdlingPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIdlingPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIdlingPolicyStatus): Boolean {
        when (status) {
            GlobalIdlingPolicyStatus.REQUESTED -> {
                when (next) {
                    GlobalIdlingPolicyStatus.GRANTED -> return true
                }
                return false
            }
            GlobalIdlingPolicyStatus.GRANTED -> return next == GlobalIdlingPolicyStatus.EXPIRED
            GlobalIdlingPolicyStatus.REVOKED -> {
                when (next) {
                    GlobalIdlingPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalIdlingPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global idling policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round(windowDays.toDouble() * score * 100) / 100

    fun snapshot(): GlobalIdlingPolicy {
        return GlobalIdlingPolicy(
            code = code,
            title = title,
            routeCode = routeCode,
            score = score,
            createdAt = createdAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global idling policy in status requested. */
fun newGlobalIdlingPolicy(code: String, title: String): GlobalIdlingPolicy {
    return GlobalIdlingPolicy(
        code = code,
        title = title,
        routeCode = "",
        score = 0.0,
        createdAt = 0,
        windowDays = 0,
        status = GlobalIdlingPolicyStatus.REQUESTED,
    )
}
