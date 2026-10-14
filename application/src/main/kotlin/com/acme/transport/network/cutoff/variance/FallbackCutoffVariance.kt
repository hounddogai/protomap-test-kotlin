package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback cutoff variance records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a fallback cutoff variance is in its life cycle. */
enum class FallbackCutoffVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback cutoff variance and its current status. */
data class FallbackCutoffVariance(
    var code: String,
    var name: String,
    var title: String,
    var phone: String,
    var endsAt: Int,
    var trailerId: String,
    var createdAt: Int,
    var depot: String,
    var startsAt: Int,
    var sequence: Int,
    var status: FallbackCutoffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCutoffVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCutoffVarianceStatus): Boolean {
        when (status) {
            FallbackCutoffVarianceStatus.NEW -> return next == FallbackCutoffVarianceStatus.DONE || next == FallbackCutoffVarianceStatus.FAILED
            FallbackCutoffVarianceStatus.QUEUED -> {
                when (next) {
                    FallbackCutoffVarianceStatus.FAILED -> return true
                }
                return false
            }
            FallbackCutoffVarianceStatus.RUNNING -> {
                when (next) {
                    FallbackCutoffVarianceStatus.DONE -> return true
                }
                return false
            }
            FallbackCutoffVarianceStatus.DONE -> {
                when (next) {
                    FallbackCutoffVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCutoffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback cutoff variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((createdAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackCutoffVariance {
        return FallbackCutoffVariance(
            code = code,
            name = name,
            title = title,
            phone = phone,
            endsAt = endsAt,
            trailerId = trailerId,
            createdAt = createdAt,
            depot = depot,
            startsAt = startsAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback cutoff variance in status new. */
fun newFallbackCutoffVariance(code: String, name: String): FallbackCutoffVariance {
    return FallbackCutoffVariance(
        code = code,
        name = name,
        title = "",
        phone = "",
        endsAt = 0,
        trailerId = "",
        createdAt = 0,
        depot = "",
        startsAt = 0,
        sequence = 0,
        status = FallbackCutoffVarianceStatus.NEW,
    )
}
