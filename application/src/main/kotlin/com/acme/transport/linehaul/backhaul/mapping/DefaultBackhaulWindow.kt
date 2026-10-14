package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default backhaul window is in its life cycle. */
enum class DefaultBackhaulWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default backhaul window, with what the planners need to act on it. */
data class DefaultBackhaulWindow(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var trailerId: String,
    var durationMinutes: Int,
    var description: String,
    var retries: Int,
    var ceiling: Double,
    var cost: Double,
    var title: String,
    var distanceKm: Double,
    var status: DefaultBackhaulWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBackhaulWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBackhaulWindowStatus): Boolean {
        when (status) {
            DefaultBackhaulWindowStatus.REQUESTED -> {
                when (next) {
                    DefaultBackhaulWindowStatus.GRANTED, DefaultBackhaulWindowStatus.REVOKED -> return true
                }
                return false
            }
            DefaultBackhaulWindowStatus.GRANTED -> {
                when (next) {
                    DefaultBackhaulWindowStatus.REVOKED, DefaultBackhaulWindowStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultBackhaulWindowStatus.REVOKED -> {
                when (next) {
                    DefaultBackhaulWindowStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultBackhaulWindowStatus) {
        if (!canMoveTo(next)) {
            error("default backhaul window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(durationMinutes.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): DefaultBackhaulWindow {
        return DefaultBackhaulWindow(
            code = code,
            label = label,
            updatedAt = updatedAt,
            trailerId = trailerId,
            durationMinutes = durationMinutes,
            description = description,
            retries = retries,
            ceiling = ceiling,
            cost = cost,
            title = title,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default backhaul window in status requested. */
fun newDefaultBackhaulWindow(code: String, label: String): DefaultBackhaulWindow {
    return DefaultBackhaulWindow(
        code = code,
        label = label,
        updatedAt = 0,
        trailerId = "",
        durationMinutes = 0,
        description = "",
        retries = 0,
        ceiling = 0.0,
        cost = 0.0,
        title = "",
        distanceKm = 0.0,
        status = DefaultBackhaulWindowStatus.REQUESTED,
    )
}
