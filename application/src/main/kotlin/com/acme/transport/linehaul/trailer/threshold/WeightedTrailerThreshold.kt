package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted trailer threshold records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a weighted trailer threshold is in its life cycle. */
enum class WeightedTrailerThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted trailer threshold. Its code is unique within a tenant. */
data class WeightedTrailerThreshold(
    var code: String,
    var name: String,
    var ratio: Double,
    var expiresAt: Int,
    var ownerId: String,
    var score: Double,
    var maximum: Double,
    var priority: Int,
    var label: String,
    var margin: Double,
    var pinned: Boolean,
    var status: WeightedTrailerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerThresholdStatus): Boolean {
        when (status) {
            WeightedTrailerThresholdStatus.REQUESTED -> {
                when (next) {
                    WeightedTrailerThresholdStatus.GRANTED -> return true
                }
                return false
            }
            WeightedTrailerThresholdStatus.GRANTED -> {
                when (next) {
                    WeightedTrailerThresholdStatus.REVOKED, WeightedTrailerThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedTrailerThresholdStatus.REVOKED -> return next == WeightedTrailerThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedTrailerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun netTotal(): Double = round(priority.toDouble() * margin * 100) / 100

    fun snapshot(): WeightedTrailerThreshold {
        return WeightedTrailerThreshold(
            code = code,
            name = name,
            ratio = ratio,
            expiresAt = expiresAt,
            ownerId = ownerId,
            score = score,
            maximum = maximum,
            priority = priority,
            label = label,
            margin = margin,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted trailer threshold in status requested. */
fun newWeightedTrailerThreshold(code: String, name: String): WeightedTrailerThreshold {
    return WeightedTrailerThreshold(
        code = code,
        name = name,
        ratio = 0.0,
        expiresAt = 0,
        ownerId = "",
        score = 0.0,
        maximum = 0.0,
        priority = 0,
        label = "",
        margin = 0.0,
        pinned = false,
        status = WeightedTrailerThresholdStatus.REQUESTED,
    )
}
