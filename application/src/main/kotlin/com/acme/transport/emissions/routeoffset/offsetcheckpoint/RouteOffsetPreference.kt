package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset preference is in its life cycle. */
enum class RouteOffsetPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a route offset preference and its current status. */
data class RouteOffsetPreference(
    var code: String,
    var label: String,
    var attempts: Int,
    var contactEmail: String,
    var distanceKm: Double,
    var stopCount: Int,
    var graceDays: Int,
    var depot: String,
    var country: String,
    var createdAt: Int,
    var loadKg: Double,
    var status: RouteOffsetPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetPreferenceStatus): Boolean {
        when (status) {
            RouteOffsetPreferenceStatus.IDLE -> {
                when (next) {
                    RouteOffsetPreferenceStatus.DRAINING -> return true
                }
                return false
            }
            RouteOffsetPreferenceStatus.WARMING -> {
                when (next) {
                    RouteOffsetPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            RouteOffsetPreferenceStatus.READY -> return next == RouteOffsetPreferenceStatus.DRAINING || next == RouteOffsetPreferenceStatus.STOPPED
            RouteOffsetPreferenceStatus.DRAINING -> return next == RouteOffsetPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RouteOffsetPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("route offset preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round(createdAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): RouteOffsetPreference {
        return RouteOffsetPreference(
            code = code,
            label = label,
            attempts = attempts,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            stopCount = stopCount,
            graceDays = graceDays,
            depot = depot,
            country = country,
            createdAt = createdAt,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a route offset preference in status idle. */
fun newRouteOffsetPreference(code: String, label: String): RouteOffsetPreference {
    return RouteOffsetPreference(
        code = code,
        label = label,
        attempts = 0,
        contactEmail = "",
        distanceKm = 0.0,
        stopCount = 0,
        graceDays = 0,
        depot = "",
        country = "",
        createdAt = 0,
        loadKg = 0.0,
        status = RouteOffsetPreferenceStatus.IDLE,
    )
}
