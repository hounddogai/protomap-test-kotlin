package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery policy is in its life cycle. */
enum class RedeliveryPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One redelivery policy, with what the planners need to act on it. */
data class RedeliveryPolicy(
    var code: String,
    var title: String,
    var driverId: String,
    var archived: Boolean,
    var maximum: Double,
    var phone: String,
    var source: String,
    var summary: String,
    var startsAt: Int,
    var capacity: Int,
    var status: RedeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryPolicyStatus): Boolean {
        when (status) {
            RedeliveryPolicyStatus.NEW -> {
                when (next) {
                    RedeliveryPolicyStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryPolicyStatus.QUEUED -> return next == RedeliveryPolicyStatus.DONE || next == RedeliveryPolicyStatus.FAILED
            RedeliveryPolicyStatus.RUNNING -> {
                when (next) {
                    RedeliveryPolicyStatus.DONE, RedeliveryPolicyStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryPolicyStatus.DONE -> {
                when (next) {
                    RedeliveryPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("redelivery policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"redelivery policy"} $code"

    fun weightedValue(): Double = round((maximum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryPolicy {
        return RedeliveryPolicy(
            code = code,
            title = title,
            driverId = driverId,
            archived = archived,
            maximum = maximum,
            phone = phone,
            source = source,
            summary = summary,
            startsAt = startsAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a redelivery policy in status new. */
fun newRedeliveryPolicy(code: String, title: String): RedeliveryPolicy {
    return RedeliveryPolicy(
        code = code,
        title = title,
        driverId = "",
        archived = false,
        maximum = 0.0,
        phone = "",
        source = "",
        summary = "",
        startsAt = 0,
        capacity = 0,
        status = RedeliveryPolicyStatus.NEW,
    )
}
