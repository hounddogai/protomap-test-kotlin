package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default idling rollup is in its life cycle. */
enum class DefaultIdlingRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default idling rollup, with what the planners need to act on it. */
data class DefaultIdlingRollup(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var retries: Int,
    var checksum: String,
    var minimum: Double,
    var contactEmail: String,
    var vehicleId: String,
    var channel: String,
    var windowDays: Int,
    var currency: String,
    var status: DefaultIdlingRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIdlingRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIdlingRollupStatus): Boolean {
        when (status) {
            DefaultIdlingRollupStatus.NEW -> return next == DefaultIdlingRollupStatus.DONE || next == DefaultIdlingRollupStatus.FAILED
            DefaultIdlingRollupStatus.QUEUED -> return next == DefaultIdlingRollupStatus.RUNNING || next == DefaultIdlingRollupStatus.FAILED
            DefaultIdlingRollupStatus.RUNNING -> return next == DefaultIdlingRollupStatus.DONE || next == DefaultIdlingRollupStatus.FAILED
            DefaultIdlingRollupStatus.DONE -> {
                when (next) {
                    DefaultIdlingRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultIdlingRollupStatus) {
        if (!canMoveTo(next)) {
            error("default idling rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round(windowDays.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): DefaultIdlingRollup {
        return DefaultIdlingRollup(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            retries = retries,
            checksum = checksum,
            minimum = minimum,
            contactEmail = contactEmail,
            vehicleId = vehicleId,
            channel = channel,
            windowDays = windowDays,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default idling rollup in status new. */
fun newDefaultIdlingRollup(code: String, title: String): DefaultIdlingRollup {
    return DefaultIdlingRollup(
        code = code,
        title = title,
        etaMinutes = 0,
        retries = 0,
        checksum = "",
        minimum = 0.0,
        contactEmail = "",
        vehicleId = "",
        channel = "",
        windowDays = 0,
        currency = "",
        status = DefaultIdlingRollupStatus.NEW,
    )
}
