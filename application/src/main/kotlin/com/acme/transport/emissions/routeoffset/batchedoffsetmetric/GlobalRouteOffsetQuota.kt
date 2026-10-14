package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global route offset quota is in its life cycle. */
enum class GlobalRouteOffsetQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global route offset quota. Its code is unique within a tenant. */
data class GlobalRouteOffsetQuota(
    var code: String,
    var label: String,
    var retries: Int,
    var threshold: Double,
    var endsAt: Int,
    var reviewer: String,
    var lane: String,
    var region: String,
    var routeCode: String,
    var pinned: Boolean,
    var status: GlobalRouteOffsetQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRouteOffsetQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRouteOffsetQuotaStatus): Boolean {
        when (status) {
            GlobalRouteOffsetQuotaStatus.REQUESTED -> return next == GlobalRouteOffsetQuotaStatus.EXPIRED
            GlobalRouteOffsetQuotaStatus.GRANTED -> {
                when (next) {
                    GlobalRouteOffsetQuotaStatus.REVOKED, GlobalRouteOffsetQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalRouteOffsetQuotaStatus.REVOKED -> {
                when (next) {
                    GlobalRouteOffsetQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRouteOffsetQuotaStatus) {
        if (!canMoveTo(next)) {
            error("global route offset quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global route offset quota"} $code"

    fun netTotal(): Double = round((retries.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalRouteOffsetQuota {
        return GlobalRouteOffsetQuota(
            code = code,
            label = label,
            retries = retries,
            threshold = threshold,
            endsAt = endsAt,
            reviewer = reviewer,
            lane = lane,
            region = region,
            routeCode = routeCode,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global route offset quota in status requested. */
fun newGlobalRouteOffsetQuota(code: String, label: String): GlobalRouteOffsetQuota {
    return GlobalRouteOffsetQuota(
        code = code,
        label = label,
        retries = 0,
        threshold = 0.0,
        endsAt = 0,
        reviewer = "",
        lane = "",
        region = "",
        routeCode = "",
        pinned = false,
        status = GlobalRouteOffsetQuotaStatus.REQUESTED,
    )
}
