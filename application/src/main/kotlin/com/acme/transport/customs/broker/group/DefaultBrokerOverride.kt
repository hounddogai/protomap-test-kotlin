package com.acme.transport.customs.broker.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default broker override is in its life cycle. */
enum class DefaultBrokerOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default broker override, with what the planners need to act on it. */
data class DefaultBrokerOverride(
    var code: String,
    var label: String,
    var revision: Int,
    var startsAt: Int,
    var channel: String,
    var fuelLiters: Double,
    var notes: String,
    var discount: Double,
    var limit: Int,
    var timezone: String,
    var endsAt: Int,
    var status: DefaultBrokerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBrokerOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBrokerOverrideStatus): Boolean {
        when (status) {
            DefaultBrokerOverrideStatus.NEW -> {
                when (next) {
                    DefaultBrokerOverrideStatus.FAILED -> return true
                }
                return false
            }
            DefaultBrokerOverrideStatus.QUEUED -> {
                when (next) {
                    DefaultBrokerOverrideStatus.RUNNING, DefaultBrokerOverrideStatus.DONE -> return true
                }
                return false
            }
            DefaultBrokerOverrideStatus.RUNNING -> {
                when (next) {
                    DefaultBrokerOverrideStatus.DONE, DefaultBrokerOverrideStatus.FAILED -> return true
                }
                return false
            }
            DefaultBrokerOverrideStatus.DONE -> return next == DefaultBrokerOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultBrokerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("default broker override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round((discount + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultBrokerOverride {
        return DefaultBrokerOverride(
            code = code,
            label = label,
            revision = revision,
            startsAt = startsAt,
            channel = channel,
            fuelLiters = fuelLiters,
            notes = notes,
            discount = discount,
            limit = limit,
            timezone = timezone,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default broker override in status new. */
fun newDefaultBrokerOverride(code: String, label: String): DefaultBrokerOverride {
    return DefaultBrokerOverride(
        code = code,
        label = label,
        revision = 0,
        startsAt = 0,
        channel = "",
        fuelLiters = 0.0,
        notes = "",
        discount = 0.0,
        limit = 0,
        timezone = "",
        endsAt = 0,
        status = DefaultBrokerOverrideStatus.NEW,
    )
}
