package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service plan notice is in its life cycle. */
enum class FallbackServicePlanNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback service plan notice as the service plan team keeps it. */
data class FallbackServicePlanNotice(
    var code: String,
    var title: String,
    var rate: Double,
    var ownerId: String,
    var tags: MutableList<String>,
    var durationMinutes: Int,
    var description: String,
    var archived: Boolean,
    var segment: String,
    var odometer: Int,
    var carrier: String,
    var status: FallbackServicePlanNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServicePlanNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServicePlanNoticeStatus): Boolean {
        when (status) {
            FallbackServicePlanNoticeStatus.REQUESTED -> {
                when (next) {
                    FallbackServicePlanNoticeStatus.GRANTED, FallbackServicePlanNoticeStatus.REVOKED -> return true
                }
                return false
            }
            FallbackServicePlanNoticeStatus.GRANTED -> {
                when (next) {
                    FallbackServicePlanNoticeStatus.REVOKED, FallbackServicePlanNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackServicePlanNoticeStatus.REVOKED -> return next == FallbackServicePlanNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackServicePlanNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback service plan notice $code cannot move from $status to $next")
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

    fun projectedLoad(): Double = round((odometer.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackServicePlanNotice {
        return FallbackServicePlanNotice(
            code = code,
            title = title,
            rate = rate,
            ownerId = ownerId,
            tags = tags,
            durationMinutes = durationMinutes,
            description = description,
            archived = archived,
            segment = segment,
            odometer = odometer,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback service plan notice in status requested. */
fun newFallbackServicePlanNotice(code: String, title: String): FallbackServicePlanNotice {
    return FallbackServicePlanNotice(
        code = code,
        title = title,
        rate = 0.0,
        ownerId = "",
        tags = mutableListOf<String>(),
        durationMinutes = 0,
        description = "",
        archived = false,
        segment = "",
        odometer = 0,
        carrier = "",
        status = FallbackServicePlanNoticeStatus.REQUESTED,
    )
}
