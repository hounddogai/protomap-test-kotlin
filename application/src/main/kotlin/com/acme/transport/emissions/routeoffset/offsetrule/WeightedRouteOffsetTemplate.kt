package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted route offset template is in its life cycle. */
enum class WeightedRouteOffsetTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One weighted route offset template, with what the planners need to act on it. */
data class WeightedRouteOffsetTemplate(
    var code: String,
    var title: String,
    var budget: Double,
    var odometer: Int,
    var tags: MutableList<String>,
    var graceDays: Int,
    var rate: Double,
    var status: WeightedRouteOffsetTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRouteOffsetTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRouteOffsetTemplateStatus): Boolean {
        when (status) {
            WeightedRouteOffsetTemplateStatus.DRAFT -> return next == WeightedRouteOffsetTemplateStatus.PENDING
            WeightedRouteOffsetTemplateStatus.PENDING -> return next == WeightedRouteOffsetTemplateStatus.CLOSED
            WeightedRouteOffsetTemplateStatus.ACTIVE -> return next == WeightedRouteOffsetTemplateStatus.SUSPENDED || next == WeightedRouteOffsetTemplateStatus.CLOSED
            WeightedRouteOffsetTemplateStatus.SUSPENDED -> {
                when (next) {
                    WeightedRouteOffsetTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRouteOffsetTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted route offset template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted route offset template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((graceDays.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): WeightedRouteOffsetTemplate {
        return WeightedRouteOffsetTemplate(
            code = code,
            title = title,
            budget = budget,
            odometer = odometer,
            tags = tags,
            graceDays = graceDays,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted route offset template in status draft. */
fun newWeightedRouteOffsetTemplate(code: String, title: String): WeightedRouteOffsetTemplate {
    return WeightedRouteOffsetTemplate(
        code = code,
        title = title,
        budget = 0.0,
        odometer = 0,
        tags = mutableListOf<String>(),
        graceDays = 0,
        rate = 0.0,
        status = WeightedRouteOffsetTemplateStatus.DRAFT,
    )
}
