package com.acme.transport.emissions.emissionfactor.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor review is in its life cycle. */
enum class EmissionFactorReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A emission factor review as the emission factor team keeps it. */
data class EmissionFactorReview(
    var code: String,
    var name: String,
    var archived: Boolean,
    var endsAt: Int,
    var retries: Int,
    var country: String,
    var loadKg: Double,
    var carrier: String,
    var label: String,
    var score: Double,
    var contactEmail: String,
    var status: EmissionFactorReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorReviewStatus): Boolean {
        when (status) {
            EmissionFactorReviewStatus.NEW -> return next == EmissionFactorReviewStatus.QUEUED || next == EmissionFactorReviewStatus.RUNNING
            EmissionFactorReviewStatus.QUEUED -> {
                when (next) {
                    EmissionFactorReviewStatus.FAILED -> return true
                }
                return false
            }
            EmissionFactorReviewStatus.RUNNING -> {
                when (next) {
                    EmissionFactorReviewStatus.FAILED -> return true
                }
                return false
            }
            EmissionFactorReviewStatus.DONE -> {
                when (next) {
                    EmissionFactorReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorReviewStatus) {
        if (!canMoveTo(next)) {
            error("emission factor review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"emission factor review"} $code"

    fun netLoad(): Double = round((score + loadKg) * 100) / 100

    fun snapshot(): EmissionFactorReview {
        return EmissionFactorReview(
            code = code,
            name = name,
            archived = archived,
            endsAt = endsAt,
            retries = retries,
            country = country,
            loadKg = loadKg,
            carrier = carrier,
            label = label,
            score = score,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a emission factor review in status new. */
fun newEmissionFactorReview(code: String, name: String): EmissionFactorReview {
    return EmissionFactorReview(
        code = code,
        name = name,
        archived = false,
        endsAt = 0,
        retries = 0,
        country = "",
        loadKg = 0.0,
        carrier = "",
        label = "",
        score = 0.0,
        contactEmail = "",
        status = EmissionFactorReviewStatus.NEW,
    )
}
