package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling policy is in its life cycle. */
enum class IdlingPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A idling policy. Its code is unique within a tenant. */
data class IdlingPolicy(
    var code: String,
    var title: String,
    var ceiling: Double,
    var threshold: Double,
    var unit: String,
    var contactEmail: String,
    var segment: String,
    var status: IdlingPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingPolicyStatus): Boolean {
        when (status) {
            IdlingPolicyStatus.NEW -> {
                when (next) {
                    IdlingPolicyStatus.DONE -> return true
                }
                return false
            }
            IdlingPolicyStatus.QUEUED -> {
                when (next) {
                    IdlingPolicyStatus.RUNNING -> return true
                }
                return false
            }
            IdlingPolicyStatus.RUNNING -> return next == IdlingPolicyStatus.DONE || next == IdlingPolicyStatus.FAILED
            IdlingPolicyStatus.DONE -> {
                when (next) {
                    IdlingPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingPolicyStatus) {
        if (!canMoveTo(next)) {
            error("idling policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((threshold - ceiling) * 100) / 100

    fun snapshot(): IdlingPolicy {
        return IdlingPolicy(
            code = code,
            title = title,
            ceiling = ceiling,
            threshold = threshold,
            unit = unit,
            contactEmail = contactEmail,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a idling policy in status new. */
fun newIdlingPolicy(code: String, title: String): IdlingPolicy {
    return IdlingPolicy(
        code = code,
        title = title,
        ceiling = 0.0,
        threshold = 0.0,
        unit = "",
        contactEmail = "",
        segment = "",
        status = IdlingPolicyStatus.NEW,
    )
}
