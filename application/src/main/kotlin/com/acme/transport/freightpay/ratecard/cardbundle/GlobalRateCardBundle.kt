package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global rate card bundle records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a global rate card bundle is in its life cycle. */
enum class GlobalRateCardBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One global rate card bundle, with what the planners need to act on it. */
data class GlobalRateCardBundle(
    var code: String,
    var title: String,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var summary: String,
    var score: Double,
    var odometer: Int,
    var rate: Double,
    var timezone: String,
    var status: GlobalRateCardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRateCardBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRateCardBundleStatus): Boolean {
        when (status) {
            GlobalRateCardBundleStatus.DRAFT -> {
                when (next) {
                    GlobalRateCardBundleStatus.PENDING -> return true
                }
                return false
            }
            GlobalRateCardBundleStatus.PENDING -> return next == GlobalRateCardBundleStatus.ACTIVE
            GlobalRateCardBundleStatus.ACTIVE -> {
                when (next) {
                    GlobalRateCardBundleStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalRateCardBundleStatus.SUSPENDED -> return next == GlobalRateCardBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalRateCardBundleStatus) {
        if (!canMoveTo(next)) {
            error("global rate card bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((score + rate) * 100) / 100

    fun snapshot(): GlobalRateCardBundle {
        return GlobalRateCardBundle(
            code = code,
            title = title,
            channel = channel,
            attributes = attributes,
            threshold = threshold,
            summary = summary,
            score = score,
            odometer = odometer,
            rate = rate,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global rate card bundle in status draft. */
fun newGlobalRateCardBundle(code: String, title: String): GlobalRateCardBundle {
    return GlobalRateCardBundle(
        code = code,
        title = title,
        channel = "",
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        summary = "",
        score = 0.0,
        odometer = 0,
        rate = 0.0,
        timezone = "",
        status = GlobalRateCardBundleStatus.DRAFT,
    )
}
