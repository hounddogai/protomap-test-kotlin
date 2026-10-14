package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset template is in its life cycle. */
enum class RouteOffsetTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One route offset template, with what the planners need to act on it. */
data class RouteOffsetTemplate(
    var code: String,
    var name: String,
    var odometer: Int,
    var visible: Boolean,
    var score: Double,
    var locale: String,
    var retries: Int,
    var approved: Boolean,
    var fuelLiters: Double,
    var status: RouteOffsetTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetTemplateStatus): Boolean {
        when (status) {
            RouteOffsetTemplateStatus.NEW -> {
                when (next) {
                    RouteOffsetTemplateStatus.QUEUED, RouteOffsetTemplateStatus.DONE -> return true
                }
                return false
            }
            RouteOffsetTemplateStatus.QUEUED -> return next == RouteOffsetTemplateStatus.DONE
            RouteOffsetTemplateStatus.RUNNING -> return next == RouteOffsetTemplateStatus.FAILED
            RouteOffsetTemplateStatus.DONE -> {
                when (next) {
                    RouteOffsetTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetTemplateStatus) {
        if (!canMoveTo(next)) {
            error("route offset template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round(retries.toDouble() * score * 100) / 100

    fun snapshot(): RouteOffsetTemplate {
        return RouteOffsetTemplate(
            code = code,
            name = name,
            odometer = odometer,
            visible = visible,
            score = score,
            locale = locale,
            retries = retries,
            approved = approved,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a route offset template in status new. */
fun newRouteOffsetTemplate(code: String, name: String): RouteOffsetTemplate {
    return RouteOffsetTemplate(
        code = code,
        name = name,
        odometer = 0,
        visible = false,
        score = 0.0,
        locale = "",
        retries = 0,
        approved = false,
        fuelLiters = 0.0,
        status = RouteOffsetTemplateStatus.NEW,
    )
}
