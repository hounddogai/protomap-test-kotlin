package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default redelivery bundle is in its life cycle. */
enum class DefaultRedeliveryBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default redelivery bundle as the redelivery team keeps it. */
data class DefaultRedeliveryBundle(
    var code: String,
    var name: String,
    var routeCode: String,
    var ceiling: Double,
    var lane: String,
    var graceDays: Int,
    var attempts: Int,
    var startsAt: Int,
    var archived: Boolean,
    var contactEmail: String,
    var updatedAt: Int,
    var status: DefaultRedeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRedeliveryBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRedeliveryBundleStatus): Boolean {
        when (status) {
            DefaultRedeliveryBundleStatus.IDLE -> return next == DefaultRedeliveryBundleStatus.DRAINING
            DefaultRedeliveryBundleStatus.WARMING -> {
                when (next) {
                    DefaultRedeliveryBundleStatus.READY -> return true
                }
                return false
            }
            DefaultRedeliveryBundleStatus.READY -> return next == DefaultRedeliveryBundleStatus.STOPPED
            DefaultRedeliveryBundleStatus.DRAINING -> return next == DefaultRedeliveryBundleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultRedeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("default redelivery bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((updatedAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultRedeliveryBundle {
        return DefaultRedeliveryBundle(
            code = code,
            name = name,
            routeCode = routeCode,
            ceiling = ceiling,
            lane = lane,
            graceDays = graceDays,
            attempts = attempts,
            startsAt = startsAt,
            archived = archived,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default redelivery bundle in status idle. */
fun newDefaultRedeliveryBundle(code: String, name: String): DefaultRedeliveryBundle {
    return DefaultRedeliveryBundle(
        code = code,
        name = name,
        routeCode = "",
        ceiling = 0.0,
        lane = "",
        graceDays = 0,
        attempts = 0,
        startsAt = 0,
        archived = false,
        contactEmail = "",
        updatedAt = 0,
        status = DefaultRedeliveryBundleStatus.IDLE,
    )
}
