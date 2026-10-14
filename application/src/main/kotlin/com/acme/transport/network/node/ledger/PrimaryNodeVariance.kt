package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary node variance is in its life cycle. */
enum class PrimaryNodeVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary node variance as the node team keeps it. */
data class PrimaryNodeVariance(
    var code: String,
    var title: String,
    var active: Boolean,
    var etaMinutes: Int,
    var distanceKm: Double,
    var description: String,
    var createdAt: Int,
    var capacity: Int,
    var status: PrimaryNodeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNodeVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNodeVarianceStatus): Boolean {
        when (status) {
            PrimaryNodeVarianceStatus.OPEN -> {
                when (next) {
                    PrimaryNodeVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryNodeVarianceStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryNodeVarianceStatus.REOPENED, PrimaryNodeVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryNodeVarianceStatus.RESOLVED -> return next == PrimaryNodeVarianceStatus.ARCHIVED
            PrimaryNodeVarianceStatus.REOPENED -> {
                when (next) {
                    PrimaryNodeVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNodeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("primary node variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): PrimaryNodeVariance {
        return PrimaryNodeVariance(
            code = code,
            title = title,
            active = active,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            description = description,
            createdAt = createdAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary node variance in status open. */
fun newPrimaryNodeVariance(code: String, title: String): PrimaryNodeVariance {
    return PrimaryNodeVariance(
        code = code,
        title = title,
        active = false,
        etaMinutes = 0,
        distanceKm = 0.0,
        description = "",
        createdAt = 0,
        capacity = 0,
        status = PrimaryNodeVarianceStatus.OPEN,
    )
}
