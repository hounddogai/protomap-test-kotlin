package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card schedule is in its life cycle. */
enum class RateCardScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rate card schedule, with what the planners need to act on it. */
data class RateCardSchedule(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var reviewer: String,
    var capacity: Int,
    var status: RateCardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardScheduleStatus): Boolean {
        when (status) {
            RateCardScheduleStatus.DRAFT -> {
                when (next) {
                    RateCardScheduleStatus.PENDING, RateCardScheduleStatus.ACTIVE -> return true
                }
                return false
            }
            RateCardScheduleStatus.PENDING -> {
                when (next) {
                    RateCardScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            RateCardScheduleStatus.ACTIVE -> {
                when (next) {
                    RateCardScheduleStatus.SUSPENDED, RateCardScheduleStatus.CLOSED -> return true
                }
                return false
            }
            RateCardScheduleStatus.SUSPENDED -> {
                when (next) {
                    RateCardScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("rate card schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((capacity.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): RateCardSchedule {
        return RateCardSchedule(
            code = code,
            title = title,
            attributes = attributes,
            endsAt = endsAt,
            reviewer = reviewer,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card schedule in status draft. */
fun newRateCardSchedule(code: String, title: String): RateCardSchedule {
    return RateCardSchedule(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        reviewer = "",
        capacity = 0,
        status = RateCardScheduleStatus.DRAFT,
    )
}
