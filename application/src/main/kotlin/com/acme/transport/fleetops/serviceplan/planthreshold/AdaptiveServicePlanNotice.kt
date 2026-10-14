package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive service plan notice is in its life cycle. */
enum class AdaptiveServicePlanNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive service plan notice as the service plan team keeps it. */
data class AdaptiveServicePlanNotice(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: Double,
    var expiresAt: Int,
    var region: String,
    var depot: String,
    var dock: String,
    var status: AdaptiveServicePlanNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServicePlanNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServicePlanNoticeStatus): Boolean {
        when (status) {
            AdaptiveServicePlanNoticeStatus.DRAFT -> return next == AdaptiveServicePlanNoticeStatus.ACTIVE
            AdaptiveServicePlanNoticeStatus.PENDING -> {
                when (next) {
                    AdaptiveServicePlanNoticeStatus.ACTIVE, AdaptiveServicePlanNoticeStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveServicePlanNoticeStatus.ACTIVE -> return next == AdaptiveServicePlanNoticeStatus.SUSPENDED || next == AdaptiveServicePlanNoticeStatus.CLOSED
            AdaptiveServicePlanNoticeStatus.SUSPENDED -> return next == AdaptiveServicePlanNoticeStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveServicePlanNoticeStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service plan notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round((expiresAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): AdaptiveServicePlanNotice {
        return AdaptiveServicePlanNotice(
            code = code,
            label = label,
            locale = locale,
            threshold = threshold,
            expiresAt = expiresAt,
            region = region,
            depot = depot,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive service plan notice in status draft. */
fun newAdaptiveServicePlanNotice(code: String, label: String): AdaptiveServicePlanNotice {
    return AdaptiveServicePlanNotice(
        code = code,
        label = label,
        locale = "",
        threshold = 0.0,
        expiresAt = 0,
        region = "",
        depot = "",
        dock = "",
        status = AdaptiveServicePlanNoticeStatus.DRAFT,
    )
}
