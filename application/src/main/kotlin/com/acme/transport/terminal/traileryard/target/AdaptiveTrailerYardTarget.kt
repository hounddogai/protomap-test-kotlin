package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive trailer yard target records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a adaptive trailer yard target is in its life cycle. */
enum class AdaptiveTrailerYardTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive trailer yard target. Its code is unique within a tenant. */
data class AdaptiveTrailerYardTarget(
    var code: String,
    var title: String,
    var externalRef: String,
    var startsAt: Int,
    var locale: String,
    var ratio: Double,
    var minimum: Double,
    var priority: Int,
    var category: String,
    var status: AdaptiveTrailerYardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerYardTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerYardTargetStatus): Boolean {
        when (status) {
            AdaptiveTrailerYardTargetStatus.PLANNED -> {
                when (next) {
                    AdaptiveTrailerYardTargetStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveTrailerYardTargetStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveTrailerYardTargetStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveTrailerYardTargetStatus.IN_PROGRESS -> return next == AdaptiveTrailerYardTargetStatus.COMPLETED || next == AdaptiveTrailerYardTargetStatus.CANCELLED
            AdaptiveTrailerYardTargetStatus.COMPLETED -> {
                when (next) {
                    AdaptiveTrailerYardTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerYardTargetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer yard target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((startsAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): AdaptiveTrailerYardTarget {
        return AdaptiveTrailerYardTarget(
            code = code,
            title = title,
            externalRef = externalRef,
            startsAt = startsAt,
            locale = locale,
            ratio = ratio,
            minimum = minimum,
            priority = priority,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive trailer yard target in status planned. */
fun newAdaptiveTrailerYardTarget(code: String, title: String): AdaptiveTrailerYardTarget {
    return AdaptiveTrailerYardTarget(
        code = code,
        title = title,
        externalRef = "",
        startsAt = 0,
        locale = "",
        ratio = 0.0,
        minimum = 0.0,
        priority = 0,
        category = "",
        status = AdaptiveTrailerYardTargetStatus.PLANNED,
    )
}
