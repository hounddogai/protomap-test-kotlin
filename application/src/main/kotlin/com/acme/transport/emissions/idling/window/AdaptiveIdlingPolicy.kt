package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive idling policy is in its life cycle. */
enum class AdaptiveIdlingPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive idling policy. Its code is unique within a tenant. */
data class AdaptiveIdlingPolicy(
    var code: String,
    var title: String,
    var channel: String,
    var maximum: Double,
    var startsAt: Int,
    var score: Double,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var createdAt: Int,
    var discount: Double,
    var status: AdaptiveIdlingPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIdlingPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIdlingPolicyStatus): Boolean {
        when (status) {
            AdaptiveIdlingPolicyStatus.PLANNED -> return next == AdaptiveIdlingPolicyStatus.IN_PROGRESS
            AdaptiveIdlingPolicyStatus.SCHEDULED -> return next == AdaptiveIdlingPolicyStatus.IN_PROGRESS || next == AdaptiveIdlingPolicyStatus.COMPLETED
            AdaptiveIdlingPolicyStatus.IN_PROGRESS -> return next == AdaptiveIdlingPolicyStatus.COMPLETED || next == AdaptiveIdlingPolicyStatus.CANCELLED
            AdaptiveIdlingPolicyStatus.COMPLETED -> {
                when (next) {
                    AdaptiveIdlingPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveIdlingPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive idling policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((discount + batchSize.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveIdlingPolicy {
        return AdaptiveIdlingPolicy(
            code = code,
            title = title,
            channel = channel,
            maximum = maximum,
            startsAt = startsAt,
            score = score,
            batchSize = batchSize,
            attributes = attributes,
            createdAt = createdAt,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive idling policy in status planned. */
fun newAdaptiveIdlingPolicy(code: String, title: String): AdaptiveIdlingPolicy {
    return AdaptiveIdlingPolicy(
        code = code,
        title = title,
        channel = "",
        maximum = 0.0,
        startsAt = 0,
        score = 0.0,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        createdAt = 0,
        discount = 0.0,
        status = AdaptiveIdlingPolicyStatus.PLANNED,
    )
}
