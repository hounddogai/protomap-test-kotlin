package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling backhaul quota records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a rolling backhaul quota is in its life cycle. */
enum class RollingBackhaulQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling backhaul quota. Its code is unique within a tenant. */
data class RollingBackhaulQuota(
    var code: String,
    var label: String,
    var threshold: Double,
    var updatedAt: Int,
    var pinned: Boolean,
    var locale: String,
    var carrier: String,
    var score: Double,
    var status: RollingBackhaulQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulQuotaStatus): Boolean {
        when (status) {
            RollingBackhaulQuotaStatus.CREATED -> {
                when (next) {
                    RollingBackhaulQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingBackhaulQuotaStatus.VALIDATED -> {
                when (next) {
                    RollingBackhaulQuotaStatus.PUBLISHED, RollingBackhaulQuotaStatus.RETIRED -> return true
                }
                return false
            }
            RollingBackhaulQuotaStatus.PUBLISHED -> return next == RollingBackhaulQuotaStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingBackhaulQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round(threshold * updatedAt.toDouble() * 100) / 100

    fun snapshot(): RollingBackhaulQuota {
        return RollingBackhaulQuota(
            code = code,
            label = label,
            threshold = threshold,
            updatedAt = updatedAt,
            pinned = pinned,
            locale = locale,
            carrier = carrier,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling backhaul quota in status created. */
fun newRollingBackhaulQuota(code: String, label: String): RollingBackhaulQuota {
    return RollingBackhaulQuota(
        code = code,
        label = label,
        threshold = 0.0,
        updatedAt = 0,
        pinned = false,
        locale = "",
        carrier = "",
        score = 0.0,
        status = RollingBackhaulQuotaStatus.CREATED,
    )
}
