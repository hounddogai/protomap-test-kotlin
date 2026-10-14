package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global broker adjustment is in its life cycle. */
enum class GlobalBrokerAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global broker adjustment and its current status. */
data class GlobalBrokerAdjustment(
    var code: String,
    var label: String,
    var depot: String,
    var expiresAt: Int,
    var active: Boolean,
    var name: String,
    var country: String,
    var distanceKm: Double,
    var status: GlobalBrokerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBrokerAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBrokerAdjustmentStatus): Boolean {
        when (status) {
            GlobalBrokerAdjustmentStatus.NEW -> {
                when (next) {
                    GlobalBrokerAdjustmentStatus.DONE -> return true
                }
                return false
            }
            GlobalBrokerAdjustmentStatus.QUEUED -> {
                when (next) {
                    GlobalBrokerAdjustmentStatus.RUNNING, GlobalBrokerAdjustmentStatus.DONE -> return true
                }
                return false
            }
            GlobalBrokerAdjustmentStatus.RUNNING -> {
                when (next) {
                    GlobalBrokerAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            GlobalBrokerAdjustmentStatus.DONE -> {
                when (next) {
                    GlobalBrokerAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalBrokerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global broker adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): GlobalBrokerAdjustment {
        return GlobalBrokerAdjustment(
            code = code,
            label = label,
            depot = depot,
            expiresAt = expiresAt,
            active = active,
            name = name,
            country = country,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global broker adjustment in status new. */
fun newGlobalBrokerAdjustment(code: String, label: String): GlobalBrokerAdjustment {
    return GlobalBrokerAdjustment(
        code = code,
        label = label,
        depot = "",
        expiresAt = 0,
        active = false,
        name = "",
        country = "",
        distanceKm = 0.0,
        status = GlobalBrokerAdjustmentStatus.NEW,
    )
}
