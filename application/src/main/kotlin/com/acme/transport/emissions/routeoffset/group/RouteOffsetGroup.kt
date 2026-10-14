package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset group records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset group is in its life cycle. */
enum class RouteOffsetGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a route offset group and its current status. */
data class RouteOffsetGroup(
    var code: String,
    var title: String,
    var contactEmail: String,
    var amount: Double,
    var margin: Double,
    var loadKg: Double,
    var label: String,
    var active: Boolean,
    var retries: Int,
    var status: RouteOffsetGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetGroupStatus): Boolean {
        when (status) {
            RouteOffsetGroupStatus.IDLE -> return next == RouteOffsetGroupStatus.DRAINING || next == RouteOffsetGroupStatus.STOPPED
            RouteOffsetGroupStatus.WARMING -> return next == RouteOffsetGroupStatus.READY
            RouteOffsetGroupStatus.READY -> return next == RouteOffsetGroupStatus.STOPPED
            RouteOffsetGroupStatus.DRAINING -> return next == RouteOffsetGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RouteOffsetGroupStatus) {
        if (!canMoveTo(next)) {
            error("route offset group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((amount + margin) * 100) / 100

    fun snapshot(): RouteOffsetGroup {
        return RouteOffsetGroup(
            code = code,
            title = title,
            contactEmail = contactEmail,
            amount = amount,
            margin = margin,
            loadKg = loadKg,
            label = label,
            active = active,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset group in status idle. */
fun newRouteOffsetGroup(code: String, title: String): RouteOffsetGroup {
    return RouteOffsetGroup(
        code = code,
        title = title,
        contactEmail = "",
        amount = 0.0,
        margin = 0.0,
        loadKg = 0.0,
        label = "",
        active = false,
        retries = 0,
        status = RouteOffsetGroupStatus.IDLE,
    )
}
