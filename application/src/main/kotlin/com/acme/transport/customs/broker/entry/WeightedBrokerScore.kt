package com.acme.transport.customs.broker.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted broker score is in its life cycle. */
enum class WeightedBrokerScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted broker score. Its code is unique within a tenant. */
data class WeightedBrokerScore(
    var code: String,
    var label: String,
    var rate: Double,
    var budget: Double,
    var contactEmail: String,
    var margin: Double,
    var archived: Boolean,
    var description: String,
    var status: WeightedBrokerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBrokerScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBrokerScoreStatus): Boolean {
        when (status) {
            WeightedBrokerScoreStatus.OPEN -> return next == WeightedBrokerScoreStatus.RESOLVED
            WeightedBrokerScoreStatus.IN_REVIEW -> {
                when (next) {
                    WeightedBrokerScoreStatus.REOPENED, WeightedBrokerScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedBrokerScoreStatus.RESOLVED -> return next == WeightedBrokerScoreStatus.REOPENED
            WeightedBrokerScoreStatus.REOPENED -> {
                when (next) {
                    WeightedBrokerScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedBrokerScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted broker score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted broker score"} $code"

    fun effectiveVolume(): Double = round(margin * rate * 100) / 100

    fun snapshot(): WeightedBrokerScore {
        return WeightedBrokerScore(
            code = code,
            label = label,
            rate = rate,
            budget = budget,
            contactEmail = contactEmail,
            margin = margin,
            archived = archived,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted broker score in status open. */
fun newWeightedBrokerScore(code: String, label: String): WeightedBrokerScore {
    return WeightedBrokerScore(
        code = code,
        label = label,
        rate = 0.0,
        budget = 0.0,
        contactEmail = "",
        margin = 0.0,
        archived = false,
        description = "",
        status = WeightedBrokerScoreStatus.OPEN,
    )
}
