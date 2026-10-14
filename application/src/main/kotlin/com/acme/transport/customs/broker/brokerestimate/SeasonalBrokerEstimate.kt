package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.text.normalize

// The seasonal broker estimate records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a seasonal broker estimate is in its life cycle. */
enum class SeasonalBrokerEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal broker estimate. Its code is unique within a tenant. */
data class SeasonalBrokerEstimate(
    var code: String,
    var title: String,
    var source: String,
    var margin: Double,
    var tags: MutableList<String>,
    var reviewer: String,
    var status: SeasonalBrokerEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBrokerEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBrokerEstimateStatus): Boolean {
        when (status) {
            SeasonalBrokerEstimateStatus.CREATED -> {
                when (next) {
                    SeasonalBrokerEstimateStatus.VALIDATED, SeasonalBrokerEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalBrokerEstimateStatus.VALIDATED -> return next == SeasonalBrokerEstimateStatus.PUBLISHED || next == SeasonalBrokerEstimateStatus.RETIRED
            SeasonalBrokerEstimateStatus.PUBLISHED -> {
                when (next) {
                    SeasonalBrokerEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalBrokerEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal broker estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = margin

    fun snapshot(): SeasonalBrokerEstimate {
        return SeasonalBrokerEstimate(
            code = code,
            title = title,
            source = source,
            margin = margin,
            tags = tags,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal broker estimate in status created. */
fun newSeasonalBrokerEstimate(code: String, title: String): SeasonalBrokerEstimate {
    return SeasonalBrokerEstimate(
        code = code,
        title = title,
        source = "",
        margin = 0.0,
        tags = mutableListOf<String>(),
        reviewer = "",
        status = SeasonalBrokerEstimateStatus.CREATED,
    )
}
