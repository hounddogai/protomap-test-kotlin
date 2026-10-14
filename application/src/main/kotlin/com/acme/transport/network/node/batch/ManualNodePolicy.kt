package com.acme.transport.network.node.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual node policy is in its life cycle. */
enum class ManualNodePolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual node policy, with what the planners need to act on it. */
data class ManualNodePolicy(
    var code: String,
    var title: String,
    var limit: Int,
    var version: Int,
    var attempts: Int,
    var graceDays: Int,
    var locale: String,
    var country: String,
    var score: Double,
    var startsAt: Int,
    var status: ManualNodePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualNodePolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualNodePolicyStatus): Boolean {
        when (status) {
            ManualNodePolicyStatus.CREATED -> return next == ManualNodePolicyStatus.VALIDATED || next == ManualNodePolicyStatus.PUBLISHED
            ManualNodePolicyStatus.VALIDATED -> return next == ManualNodePolicyStatus.PUBLISHED || next == ManualNodePolicyStatus.RETIRED
            ManualNodePolicyStatus.PUBLISHED -> {
                when (next) {
                    ManualNodePolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualNodePolicyStatus) {
        if (!canMoveTo(next)) {
            error("manual node policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round(attempts.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): ManualNodePolicy {
        return ManualNodePolicy(
            code = code,
            title = title,
            limit = limit,
            version = version,
            attempts = attempts,
            graceDays = graceDays,
            locale = locale,
            country = country,
            score = score,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual node policy in status created. */
fun newManualNodePolicy(code: String, title: String): ManualNodePolicy {
    return ManualNodePolicy(
        code = code,
        title = title,
        limit = 0,
        version = 0,
        attempts = 0,
        graceDays = 0,
        locale = "",
        country = "",
        score = 0.0,
        startsAt = 0,
        status = ManualNodePolicyStatus.CREATED,
    )
}
