package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted trailer yard allocation is in its life cycle. */
enum class WeightedTrailerYardAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted trailer yard allocation as the trailer yard team keeps it. */
data class WeightedTrailerYardAllocation(
    var code: String,
    var title: String,
    var cost: Double,
    var checksum: String,
    var tags: MutableList<String>,
    var ownerId: String,
    var externalRef: String,
    var etaMinutes: Int,
    var region: String,
    var score: Double,
    var status: WeightedTrailerYardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerYardAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerYardAllocationStatus): Boolean {
        when (status) {
            WeightedTrailerYardAllocationStatus.DRAFT -> {
                when (next) {
                    WeightedTrailerYardAllocationStatus.PENDING, WeightedTrailerYardAllocationStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedTrailerYardAllocationStatus.PENDING -> {
                when (next) {
                    WeightedTrailerYardAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedTrailerYardAllocationStatus.ACTIVE -> {
                when (next) {
                    WeightedTrailerYardAllocationStatus.SUSPENDED, WeightedTrailerYardAllocationStatus.CLOSED -> return true
                }
                return false
            }
            WeightedTrailerYardAllocationStatus.SUSPENDED -> return next == WeightedTrailerYardAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedTrailerYardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer yard allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((cost - score) * 100) / 100

    fun snapshot(): WeightedTrailerYardAllocation {
        return WeightedTrailerYardAllocation(
            code = code,
            title = title,
            cost = cost,
            checksum = checksum,
            tags = tags,
            ownerId = ownerId,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            region = region,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted trailer yard allocation in status draft. */
fun newWeightedTrailerYardAllocation(code: String, title: String): WeightedTrailerYardAllocation {
    return WeightedTrailerYardAllocation(
        code = code,
        title = title,
        cost = 0.0,
        checksum = "",
        tags = mutableListOf<String>(),
        ownerId = "",
        externalRef = "",
        etaMinutes = 0,
        region = "",
        score = 0.0,
        status = WeightedTrailerYardAllocationStatus.DRAFT,
    )
}
