package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard tier is in its life cycle. */
enum class TrailerYardTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer yard tier. Its code is unique within a tenant. */
data class TrailerYardTier(
    var code: String,
    var name: String,
    var batchSize: Int,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var status: TrailerYardTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardTierStatus): Boolean {
        when (status) {
            TrailerYardTierStatus.REQUESTED -> {
                when (next) {
                    TrailerYardTierStatus.GRANTED, TrailerYardTierStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardTierStatus.GRANTED -> {
                when (next) {
                    TrailerYardTierStatus.REVOKED, TrailerYardTierStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardTierStatus.REVOKED -> return next == TrailerYardTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardTierStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard tier"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((batchSize.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): TrailerYardTier {
        return TrailerYardTier(
            code = code,
            name = name,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard tier in status requested. */
fun newTrailerYardTier(code: String, name: String): TrailerYardTier {
    return TrailerYardTier(
        code = code,
        name = name,
        batchSize = 0,
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        status = TrailerYardTierStatus.REQUESTED,
    )
}
