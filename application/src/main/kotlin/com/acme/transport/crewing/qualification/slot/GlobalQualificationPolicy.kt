package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global qualification policy is in its life cycle. */
enum class GlobalQualificationPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global qualification policy, with what the planners need to act on it. */
data class GlobalQualificationPolicy(
    var code: String,
    var title: String,
    var trailerId: String,
    var attempts: Int,
    var updatedAt: Int,
    var minimum: Double,
    var graceDays: Int,
    var status: GlobalQualificationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationPolicyStatus): Boolean {
        when (status) {
            GlobalQualificationPolicyStatus.CREATED -> {
                when (next) {
                    GlobalQualificationPolicyStatus.VALIDATED, GlobalQualificationPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalQualificationPolicyStatus.VALIDATED -> return next == GlobalQualificationPolicyStatus.PUBLISHED
            GlobalQualificationPolicyStatus.PUBLISHED -> {
                when (next) {
                    GlobalQualificationPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalQualificationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global qualification policy $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global qualification policy"} $code"

    fun weightedValue(): Double = round((graceDays.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalQualificationPolicy {
        return GlobalQualificationPolicy(
            code = code,
            title = title,
            trailerId = trailerId,
            attempts = attempts,
            updatedAt = updatedAt,
            minimum = minimum,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global qualification policy in status created. */
fun newGlobalQualificationPolicy(code: String, title: String): GlobalQualificationPolicy {
    return GlobalQualificationPolicy(
        code = code,
        title = title,
        trailerId = "",
        attempts = 0,
        updatedAt = 0,
        minimum = 0.0,
        graceDays = 0,
        status = GlobalQualificationPolicyStatus.CREATED,
    )
}
