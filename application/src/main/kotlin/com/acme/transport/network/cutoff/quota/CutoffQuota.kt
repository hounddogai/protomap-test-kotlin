package com.acme.transport.network.cutoff.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff quota records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff quota is in its life cycle. */
enum class CutoffQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cutoff quota and its current status. */
data class CutoffQuota(
    var code: String,
    var title: String,
    var revision: Int,
    var startsAt: Int,
    var threshold: Double,
    var source: String,
    var active: Boolean,
    var approved: Boolean,
    var status: CutoffQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffQuotaStatus): Boolean {
        when (status) {
            CutoffQuotaStatus.DRAFT -> {
                when (next) {
                    CutoffQuotaStatus.PENDING -> return true
                }
                return false
            }
            CutoffQuotaStatus.PENDING -> return next == CutoffQuotaStatus.ACTIVE || next == CutoffQuotaStatus.SUSPENDED
            CutoffQuotaStatus.ACTIVE -> {
                when (next) {
                    CutoffQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            CutoffQuotaStatus.SUSPENDED -> return next == CutoffQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CutoffQuotaStatus) {
        if (!canMoveTo(next)) {
            error("cutoff quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"cutoff quota"} $code"

    fun effectiveExposure(): Double = round(startsAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): CutoffQuota {
        return CutoffQuota(
            code = code,
            title = title,
            revision = revision,
            startsAt = startsAt,
            threshold = threshold,
            source = source,
            active = active,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff quota in status draft. */
fun newCutoffQuota(code: String, title: String): CutoffQuota {
    return CutoffQuota(
        code = code,
        title = title,
        revision = 0,
        startsAt = 0,
        threshold = 0.0,
        source = "",
        active = false,
        approved = false,
        status = CutoffQuotaStatus.DRAFT,
    )
}
