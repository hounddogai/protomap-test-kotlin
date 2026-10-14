package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped backhaul template is in its life cycle. */
enum class CappedBackhaulTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped backhaul template, with what the planners need to act on it. */
data class CappedBackhaulTemplate(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var fuelLiters: Double,
    var vehicleId: String,
    var pinned: Boolean,
    var region: String,
    var score: Double,
    var discount: Double,
    var expiresAt: Int,
    var status: CappedBackhaulTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulTemplateStatus): Boolean {
        when (status) {
            CappedBackhaulTemplateStatus.OPEN -> return next == CappedBackhaulTemplateStatus.ARCHIVED
            CappedBackhaulTemplateStatus.IN_REVIEW -> {
                when (next) {
                    CappedBackhaulTemplateStatus.REOPENED, CappedBackhaulTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedBackhaulTemplateStatus.RESOLVED -> {
                when (next) {
                    CappedBackhaulTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedBackhaulTemplateStatus.REOPENED -> {
                when (next) {
                    CappedBackhaulTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedBackhaulTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped backhaul template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((fuelLiters - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CappedBackhaulTemplate {
        return CappedBackhaulTemplate(
            code = code,
            title = title,
            tags = tags,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            pinned = pinned,
            region = region,
            score = score,
            discount = discount,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped backhaul template in status open. */
fun newCappedBackhaulTemplate(code: String, title: String): CappedBackhaulTemplate {
    return CappedBackhaulTemplate(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        reviewer = "",
        fuelLiters = 0.0,
        vehicleId = "",
        pinned = false,
        region = "",
        score = 0.0,
        discount = 0.0,
        expiresAt = 0,
        status = CappedBackhaulTemplateStatus.OPEN,
    )
}
