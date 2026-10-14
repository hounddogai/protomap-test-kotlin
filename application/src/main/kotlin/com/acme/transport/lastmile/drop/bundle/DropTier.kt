package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop tier is in its life cycle. */
enum class DropTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A drop tier. Its code is unique within a tenant. */
data class DropTier(
    var code: String,
    var label: String,
    var attempts: Int,
    var limit: Int,
    var updatedAt: Int,
    var minimum: Double,
    var timezone: String,
    var distanceKm: Double,
    var ratio: Double,
    var score: Double,
    var quantity: Int,
    var status: DropTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropTierStatus): Boolean {
        when (status) {
            DropTierStatus.IDLE -> {
                when (next) {
                    DropTierStatus.DRAINING -> return true
                }
                return false
            }
            DropTierStatus.WARMING -> return next == DropTierStatus.READY || next == DropTierStatus.STOPPED
            DropTierStatus.READY -> {
                when (next) {
                    DropTierStatus.DRAINING -> return true
                }
                return false
            }
            DropTierStatus.DRAINING -> return next == DropTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DropTierStatus) {
        if (!canMoveTo(next)) {
            error("drop tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round(limit.toDouble() * minimum * 100) / 100

    fun snapshot(): DropTier {
        return DropTier(
            code = code,
            label = label,
            attempts = attempts,
            limit = limit,
            updatedAt = updatedAt,
            minimum = minimum,
            timezone = timezone,
            distanceKm = distanceKm,
            ratio = ratio,
            score = score,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop tier in status idle. */
fun newDropTier(code: String, label: String): DropTier {
    return DropTier(
        code = code,
        label = label,
        attempts = 0,
        limit = 0,
        updatedAt = 0,
        minimum = 0.0,
        timezone = "",
        distanceKm = 0.0,
        ratio = 0.0,
        score = 0.0,
        quantity = 0,
        status = DropTierStatus.IDLE,
    )
}
