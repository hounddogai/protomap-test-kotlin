package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.text.normalize

/** Where a adaptive hub score is in its life cycle. */
enum class AdaptiveHubScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive hub score as the hub team keeps it. */
data class AdaptiveHubScore(
    var code: String,
    var title: String,
    var endsAt: Int,
    var tags: MutableList<String>,
    var name: String,
    var status: AdaptiveHubScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveHubScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveHubScoreStatus): Boolean {
        when (status) {
            AdaptiveHubScoreStatus.CREATED -> return next == AdaptiveHubScoreStatus.PUBLISHED || next == AdaptiveHubScoreStatus.RETIRED
            AdaptiveHubScoreStatus.VALIDATED -> return next == AdaptiveHubScoreStatus.RETIRED
            AdaptiveHubScoreStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveHubScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveHubScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive hub score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): AdaptiveHubScore {
        return AdaptiveHubScore(
            code = code,
            title = title,
            endsAt = endsAt,
            tags = tags,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive hub score in status created. */
fun newAdaptiveHubScore(code: String, title: String): AdaptiveHubScore {
    return AdaptiveHubScore(
        code = code,
        title = title,
        endsAt = 0,
        tags = mutableListOf<String>(),
        name = "",
        status = AdaptiveHubScoreStatus.CREATED,
    )
}
