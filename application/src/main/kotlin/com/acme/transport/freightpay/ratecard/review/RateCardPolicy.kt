package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card policy is in its life cycle. */
enum class RateCardPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rate card policy as the rate card team keeps it. */
data class RateCardPolicy(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var contactEmail: String,
    var stopCount: Int,
    var startsAt: Int,
    var description: String,
    var status: RateCardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardPolicyStatus): Boolean {
        when (status) {
            RateCardPolicyStatus.DRAFT -> {
                when (next) {
                    RateCardPolicyStatus.ACTIVE, RateCardPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            RateCardPolicyStatus.PENDING -> {
                when (next) {
                    RateCardPolicyStatus.ACTIVE -> return true
                }
                return false
            }
            RateCardPolicyStatus.ACTIVE -> {
                when (next) {
                    RateCardPolicyStatus.SUSPENDED, RateCardPolicyStatus.CLOSED -> return true
                }
                return false
            }
            RateCardPolicyStatus.SUSPENDED -> {
                when (next) {
                    RateCardPolicyStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rate card policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((ceiling + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RateCardPolicy {
        return RateCardPolicy(
            code = code,
            title = title,
            attributes = attributes,
            ceiling = ceiling,
            contactEmail = contactEmail,
            stopCount = stopCount,
            startsAt = startsAt,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card policy in status draft. */
fun newRateCardPolicy(code: String, title: String): RateCardPolicy {
    return RateCardPolicy(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        contactEmail = "",
        stopCount = 0,
        startsAt = 0,
        description = "",
        status = RateCardPolicyStatus.DRAFT,
    )
}
