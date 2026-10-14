package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling zone window is in its life cycle. */
enum class RollingZoneWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling zone window as the zone team keeps it. */
data class RollingZoneWindow(
    var code: String,
    var title: String,
    var limit: Int,
    var minimum: Double,
    var quantity: Int,
    var capacity: Int,
    var contactEmail: String,
    var margin: Double,
    var updatedAt: Int,
    var externalRef: String,
    var revision: Int,
    var status: RollingZoneWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingZoneWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingZoneWindowStatus): Boolean {
        when (status) {
            RollingZoneWindowStatus.DRAFT -> {
                when (next) {
                    RollingZoneWindowStatus.ACTIVE -> return true
                }
                return false
            }
            RollingZoneWindowStatus.PENDING -> {
                when (next) {
                    RollingZoneWindowStatus.ACTIVE -> return true
                }
                return false
            }
            RollingZoneWindowStatus.ACTIVE -> return next == RollingZoneWindowStatus.SUSPENDED || next == RollingZoneWindowStatus.CLOSED
            RollingZoneWindowStatus.SUSPENDED -> {
                when (next) {
                    RollingZoneWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingZoneWindowStatus) {
        if (!canMoveTo(next)) {
            error("rolling zone window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((quantity.toDouble() + margin) * 100) / 100

    fun snapshot(): RollingZoneWindow {
        return RollingZoneWindow(
            code = code,
            title = title,
            limit = limit,
            minimum = minimum,
            quantity = quantity,
            capacity = capacity,
            contactEmail = contactEmail,
            margin = margin,
            updatedAt = updatedAt,
            externalRef = externalRef,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling zone window in status draft. */
fun newRollingZoneWindow(code: String, title: String): RollingZoneWindow {
    return RollingZoneWindow(
        code = code,
        title = title,
        limit = 0,
        minimum = 0.0,
        quantity = 0,
        capacity = 0,
        contactEmail = "",
        margin = 0.0,
        updatedAt = 0,
        externalRef = "",
        revision = 0,
        status = RollingZoneWindowStatus.DRAFT,
    )
}
