package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted trailer yard tier is in its life cycle. */
enum class WeightedTrailerYardTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted trailer yard tier. Its code is unique within a tenant. */
data class WeightedTrailerYardTier(
    var code: String,
    var name: String,
    var checksum: String,
    var discount: Double,
    var channel: String,
    var stopCount: Int,
    var batchSize: Int,
    var attempts: Int,
    var createdAt: Int,
    var active: Boolean,
    var ceiling: Double,
    var status: WeightedTrailerYardTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerYardTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerYardTierStatus): Boolean {
        when (status) {
            WeightedTrailerYardTierStatus.OPEN -> return next == WeightedTrailerYardTierStatus.REOPENED
            WeightedTrailerYardTierStatus.IN_REVIEW -> {
                when (next) {
                    WeightedTrailerYardTierStatus.RESOLVED, WeightedTrailerYardTierStatus.REOPENED -> return true
                }
                return false
            }
            WeightedTrailerYardTierStatus.RESOLVED -> {
                when (next) {
                    WeightedTrailerYardTierStatus.REOPENED -> return true
                }
                return false
            }
            WeightedTrailerYardTierStatus.REOPENED -> return next == WeightedTrailerYardTierStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedTrailerYardTierStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer yard tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((discount + stopCount.toDouble()) * 100) / 100

    fun snapshot(): WeightedTrailerYardTier {
        return WeightedTrailerYardTier(
            code = code,
            name = name,
            checksum = checksum,
            discount = discount,
            channel = channel,
            stopCount = stopCount,
            batchSize = batchSize,
            attempts = attempts,
            createdAt = createdAt,
            active = active,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted trailer yard tier in status open. */
fun newWeightedTrailerYardTier(code: String, name: String): WeightedTrailerYardTier {
    return WeightedTrailerYardTier(
        code = code,
        name = name,
        checksum = "",
        discount = 0.0,
        channel = "",
        stopCount = 0,
        batchSize = 0,
        attempts = 0,
        createdAt = 0,
        active = false,
        ceiling = 0.0,
        status = WeightedTrailerYardTierStatus.OPEN,
    )
}
