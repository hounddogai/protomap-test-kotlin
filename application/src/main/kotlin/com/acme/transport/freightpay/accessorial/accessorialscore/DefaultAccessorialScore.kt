package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default accessorial score records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a default accessorial score is in its life cycle. */
enum class DefaultAccessorialScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default accessorial score, with what the planners need to act on it. */
data class DefaultAccessorialScore(
    var code: String,
    var label: String,
    var channel: String,
    var approved: Boolean,
    var dock: String,
    var distanceKm: Double,
    var priority: Int,
    var amount: Double,
    var summary: String,
    var endsAt: Int,
    var checksum: String,
    var status: DefaultAccessorialScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAccessorialScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAccessorialScoreStatus): Boolean {
        when (status) {
            DefaultAccessorialScoreStatus.CREATED -> return next == DefaultAccessorialScoreStatus.PUBLISHED || next == DefaultAccessorialScoreStatus.RETIRED
            DefaultAccessorialScoreStatus.VALIDATED -> {
                when (next) {
                    DefaultAccessorialScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultAccessorialScoreStatus.PUBLISHED -> return next == DefaultAccessorialScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultAccessorialScoreStatus) {
        if (!canMoveTo(next)) {
            error("default accessorial score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default accessorial score"} $code"

    fun projectedExposure(): Double = round((priority.toDouble() - amount) * 100) / 100

    fun snapshot(): DefaultAccessorialScore {
        return DefaultAccessorialScore(
            code = code,
            label = label,
            channel = channel,
            approved = approved,
            dock = dock,
            distanceKm = distanceKm,
            priority = priority,
            amount = amount,
            summary = summary,
            endsAt = endsAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default accessorial score in status created. */
fun newDefaultAccessorialScore(code: String, label: String): DefaultAccessorialScore {
    return DefaultAccessorialScore(
        code = code,
        label = label,
        channel = "",
        approved = false,
        dock = "",
        distanceKm = 0.0,
        priority = 0,
        amount = 0.0,
        summary = "",
        endsAt = 0,
        checksum = "",
        status = DefaultAccessorialScoreStatus.CREATED,
    )
}
