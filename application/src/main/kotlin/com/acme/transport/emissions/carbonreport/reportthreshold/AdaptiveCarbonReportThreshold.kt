package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive carbon report threshold records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a adaptive carbon report threshold is in its life cycle. */
enum class AdaptiveCarbonReportThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive carbon report threshold. Its code is unique within a tenant. */
data class AdaptiveCarbonReportThreshold(
    var code: String,
    var title: String,
    var timezone: String,
    var visible: Boolean,
    var routeCode: String,
    var limit: Int,
    var budget: Double,
    var quantity: Int,
    var endsAt: Int,
    var margin: Double,
    var phone: String,
    var status: AdaptiveCarbonReportThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCarbonReportThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCarbonReportThresholdStatus): Boolean {
        when (status) {
            AdaptiveCarbonReportThresholdStatus.DRAFT -> return next == AdaptiveCarbonReportThresholdStatus.CLOSED
            AdaptiveCarbonReportThresholdStatus.PENDING -> {
                when (next) {
                    AdaptiveCarbonReportThresholdStatus.ACTIVE -> return true
                }
                return false
            }
            AdaptiveCarbonReportThresholdStatus.ACTIVE -> {
                when (next) {
                    AdaptiveCarbonReportThresholdStatus.SUSPENDED, AdaptiveCarbonReportThresholdStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveCarbonReportThresholdStatus.SUSPENDED -> return next == AdaptiveCarbonReportThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveCarbonReportThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive carbon report threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive carbon report threshold"} $code"

    fun netLoad(): Double = round((margin + limit.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveCarbonReportThreshold {
        return AdaptiveCarbonReportThreshold(
            code = code,
            title = title,
            timezone = timezone,
            visible = visible,
            routeCode = routeCode,
            limit = limit,
            budget = budget,
            quantity = quantity,
            endsAt = endsAt,
            margin = margin,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive carbon report threshold in status draft. */
fun newAdaptiveCarbonReportThreshold(code: String, title: String): AdaptiveCarbonReportThreshold {
    return AdaptiveCarbonReportThreshold(
        code = code,
        title = title,
        timezone = "",
        visible = false,
        routeCode = "",
        limit = 0,
        budget = 0.0,
        quantity = 0,
        endsAt = 0,
        margin = 0.0,
        phone = "",
        status = AdaptiveCarbonReportThresholdStatus.DRAFT,
    )
}
