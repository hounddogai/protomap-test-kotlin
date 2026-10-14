package com.acme.transport.network.transittime.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default transit time policy is in its life cycle. */
enum class DefaultTransitTimePolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default transit time policy. Its code is unique within a tenant. */
data class DefaultTransitTimePolicy(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var distanceKm: Double,
    var endsAt: Int,
    var status: DefaultTransitTimePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimePolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimePolicyStatus): Boolean {
        when (status) {
            DefaultTransitTimePolicyStatus.NEW -> {
                when (next) {
                    DefaultTransitTimePolicyStatus.QUEUED, DefaultTransitTimePolicyStatus.RUNNING -> return true
                }
                return false
            }
            DefaultTransitTimePolicyStatus.QUEUED -> {
                when (next) {
                    DefaultTransitTimePolicyStatus.RUNNING -> return true
                }
                return false
            }
            DefaultTransitTimePolicyStatus.RUNNING -> return next == DefaultTransitTimePolicyStatus.DONE || next == DefaultTransitTimePolicyStatus.FAILED
            DefaultTransitTimePolicyStatus.DONE -> return next == DefaultTransitTimePolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimePolicyStatus) {
        if (!canMoveTo(next)) {
            error("default transit time policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((distanceKm + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultTransitTimePolicy {
        return DefaultTransitTimePolicy(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default transit time policy in status new. */
fun newDefaultTransitTimePolicy(code: String, label: String): DefaultTransitTimePolicy {
    return DefaultTransitTimePolicy(
        code = code,
        label = label,
        etaMinutes = 0,
        distanceKm = 0.0,
        endsAt = 0,
        status = DefaultTransitTimePolicyStatus.NEW,
    )
}
