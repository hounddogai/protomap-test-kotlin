package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default hold rollup is in its life cycle. */
enum class DefaultHoldRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One default hold rollup, with what the planners need to act on it. */
data class DefaultHoldRollup(
    var code: String,
    var title: String,
    var odometer: Int,
    var rate: Double,
    var etaMinutes: Int,
    var contactEmail: String,
    var summary: String,
    var channel: String,
    var status: DefaultHoldRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHoldRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHoldRollupStatus): Boolean {
        when (status) {
            DefaultHoldRollupStatus.PROPOSED -> return next == DefaultHoldRollupStatus.REJECTED || next == DefaultHoldRollupStatus.WITHDRAWN
            DefaultHoldRollupStatus.APPROVED -> return next == DefaultHoldRollupStatus.WITHDRAWN
            DefaultHoldRollupStatus.REJECTED -> {
                when (next) {
                    DefaultHoldRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultHoldRollupStatus) {
        if (!canMoveTo(next)) {
            error("default hold rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round((rate - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultHoldRollup {
        return DefaultHoldRollup(
            code = code,
            title = title,
            odometer = odometer,
            rate = rate,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            summary = summary,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default hold rollup in status proposed. */
fun newDefaultHoldRollup(code: String, title: String): DefaultHoldRollup {
    return DefaultHoldRollup(
        code = code,
        title = title,
        odometer = 0,
        rate = 0.0,
        etaMinutes = 0,
        contactEmail = "",
        summary = "",
        channel = "",
        status = DefaultHoldRollupStatus.PROPOSED,
    )
}
