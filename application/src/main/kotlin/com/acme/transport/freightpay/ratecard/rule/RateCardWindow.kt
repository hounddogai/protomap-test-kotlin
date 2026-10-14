package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card window is in its life cycle. */
enum class RateCardWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rate card window and its current status. */
data class RateCardWindow(
    var code: String,
    var name: String,
    var phone: String,
    var label: String,
    var category: String,
    var lane: String,
    var capacity: Int,
    var revision: Int,
    var priority: Int,
    var tags: MutableList<String>,
    var timezone: String,
    var status: RateCardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardWindowStatus): Boolean {
        when (status) {
            RateCardWindowStatus.REQUESTED -> {
                when (next) {
                    RateCardWindowStatus.GRANTED, RateCardWindowStatus.REVOKED -> return true
                }
                return false
            }
            RateCardWindowStatus.GRANTED -> {
                when (next) {
                    RateCardWindowStatus.EXPIRED -> return true
                }
                return false
            }
            RateCardWindowStatus.REVOKED -> return next == RateCardWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RateCardWindowStatus) {
        if (!canMoveTo(next)) {
            error("rate card window $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((capacity.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): RateCardWindow {
        return RateCardWindow(
            code = code,
            name = name,
            phone = phone,
            label = label,
            category = category,
            lane = lane,
            capacity = capacity,
            revision = revision,
            priority = priority,
            tags = tags,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rate card window in status requested. */
fun newRateCardWindow(code: String, name: String): RateCardWindow {
    return RateCardWindow(
        code = code,
        name = name,
        phone = "",
        label = "",
        category = "",
        lane = "",
        capacity = 0,
        revision = 0,
        priority = 0,
        tags = mutableListOf<String>(),
        timezone = "",
        status = RateCardWindowStatus.REQUESTED,
    )
}
