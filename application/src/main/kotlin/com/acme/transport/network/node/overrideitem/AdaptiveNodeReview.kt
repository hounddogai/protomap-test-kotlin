package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive node review is in its life cycle. */
enum class AdaptiveNodeReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive node review. Its code is unique within a tenant. */
data class AdaptiveNodeReview(
    var code: String,
    var name: String,
    var endsAt: Int,
    var notes: String,
    var budget: Double,
    var visible: Boolean,
    var channel: String,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var status: AdaptiveNodeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNodeReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNodeReviewStatus): Boolean {
        when (status) {
            AdaptiveNodeReviewStatus.CREATED -> {
                when (next) {
                    AdaptiveNodeReviewStatus.VALIDATED, AdaptiveNodeReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            AdaptiveNodeReviewStatus.VALIDATED -> return next == AdaptiveNodeReviewStatus.RETIRED
            AdaptiveNodeReviewStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveNodeReviewStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveNodeReviewStatus) {
        if (!canMoveTo(next)) {
            error("adaptive node review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((endsAt.toDouble() - budget) * 100) / 100

    fun snapshot(): AdaptiveNodeReview {
        return AdaptiveNodeReview(
            code = code,
            name = name,
            endsAt = endsAt,
            notes = notes,
            budget = budget,
            visible = visible,
            channel = channel,
            version = version,
            attributes = attributes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive node review in status created. */
fun newAdaptiveNodeReview(code: String, name: String): AdaptiveNodeReview {
    return AdaptiveNodeReview(
        code = code,
        name = name,
        endsAt = 0,
        notes = "",
        budget = 0.0,
        visible = false,
        channel = "",
        version = 0,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        status = AdaptiveNodeReviewStatus.CREATED,
    )
}
