package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive emission factor request is in its life cycle. */
enum class AdaptiveEmissionFactorRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive emission factor request as the emission factor team keeps it. */
data class AdaptiveEmissionFactorRequest(
    var code: String,
    var title: String,
    var revision: Int,
    var limit: Int,
    var threshold: Double,
    var minimum: Double,
    var score: Double,
    var createdAt: Int,
    var trailerId: String,
    var active: Boolean,
    var pinned: Boolean,
    var status: AdaptiveEmissionFactorRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorRequestStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorRequestStatus.NEW -> {
                when (next) {
                    AdaptiveEmissionFactorRequestStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorRequestStatus.QUEUED -> return next == AdaptiveEmissionFactorRequestStatus.FAILED
            AdaptiveEmissionFactorRequestStatus.RUNNING -> return next == AdaptiveEmissionFactorRequestStatus.FAILED
            AdaptiveEmissionFactorRequestStatus.DONE -> {
                when (next) {
                    AdaptiveEmissionFactorRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorRequestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((revision.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorRequest {
        return AdaptiveEmissionFactorRequest(
            code = code,
            title = title,
            revision = revision,
            limit = limit,
            threshold = threshold,
            minimum = minimum,
            score = score,
            createdAt = createdAt,
            trailerId = trailerId,
            active = active,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive emission factor request in status new. */
fun newAdaptiveEmissionFactorRequest(code: String, title: String): AdaptiveEmissionFactorRequest {
    return AdaptiveEmissionFactorRequest(
        code = code,
        title = title,
        revision = 0,
        limit = 0,
        threshold = 0.0,
        minimum = 0.0,
        score = 0.0,
        createdAt = 0,
        trailerId = "",
        active = false,
        pinned = false,
        status = AdaptiveEmissionFactorRequestStatus.NEW,
    )
}
