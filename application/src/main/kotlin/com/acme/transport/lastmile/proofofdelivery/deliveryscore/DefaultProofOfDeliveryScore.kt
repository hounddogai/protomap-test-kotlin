package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default proof of delivery score records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a default proof of delivery score is in its life cycle. */
enum class DefaultProofOfDeliveryScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default proof of delivery score as the proof of delivery team keeps it. */
data class DefaultProofOfDeliveryScore(
    var code: String,
    var title: String,
    var minimum: Double,
    var stopCount: Int,
    var reviewer: String,
    var locale: String,
    var trailerId: String,
    var archived: Boolean,
    var budget: Double,
    var dock: String,
    var etaMinutes: Int,
    var status: DefaultProofOfDeliveryScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryScoreStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryScoreStatus.PLANNED -> return next == DefaultProofOfDeliveryScoreStatus.CANCELLED
            DefaultProofOfDeliveryScoreStatus.SCHEDULED -> return next == DefaultProofOfDeliveryScoreStatus.COMPLETED
            DefaultProofOfDeliveryScoreStatus.IN_PROGRESS -> return next == DefaultProofOfDeliveryScoreStatus.CANCELLED
            DefaultProofOfDeliveryScoreStatus.COMPLETED -> {
                when (next) {
                    DefaultProofOfDeliveryScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryScoreStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round((stopCount.toDouble() - minimum) * 100) / 100

    fun snapshot(): DefaultProofOfDeliveryScore {
        return DefaultProofOfDeliveryScore(
            code = code,
            title = title,
            minimum = minimum,
            stopCount = stopCount,
            reviewer = reviewer,
            locale = locale,
            trailerId = trailerId,
            archived = archived,
            budget = budget,
            dock = dock,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default proof of delivery score in status planned. */
fun newDefaultProofOfDeliveryScore(code: String, title: String): DefaultProofOfDeliveryScore {
    return DefaultProofOfDeliveryScore(
        code = code,
        title = title,
        minimum = 0.0,
        stopCount = 0,
        reviewer = "",
        locale = "",
        trailerId = "",
        archived = false,
        budget = 0.0,
        dock = "",
        etaMinutes = 0,
        status = DefaultProofOfDeliveryScoreStatus.PLANNED,
    )
}
